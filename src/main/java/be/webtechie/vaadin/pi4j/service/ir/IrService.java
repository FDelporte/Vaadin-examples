package be.webtechie.vaadin.pi4j.service.ir;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.IrCodeEvent;
import be.webtechie.vaadin.pi4j.event.IrTriggerChangedEvent;
import be.webtechie.vaadin.pi4j.event.KeyStateEvent;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.joystick.JoystickService;
import be.webtechie.vaadin.pi4j.service.oled.OledService;
import be.webtechie.vaadin.pi4j.views.electronics.IrReceiverView;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for reading IR (infrared) remote signals.
 * Supports NEC and Sony SIRC protocols.
 * Displays last code on OLED and allows KEY press to assign buzzer trigger.
 */
@Service
public class IrService {

    private static final Logger logger = LoggerFactory.getLogger(IrService.class);
    private static final int IR_PIN = 18; // GPIO 18 (BCM)

    // Timing constants in microseconds
    private static final int SAMPLE_PERIOD_US = 50; // Sample every 50 microseconds

    private final ApplicationEventPublisher eventPublisher;
    private final JoystickService joystickService;
    private final OledService oledService;
    private final DigitalInput irInput;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger activeViewCount = new AtomicInteger(0);
    private final boolean mockMode;
    private final boolean available;

    // Buzzer trigger configuration
    private final AtomicInteger buzzerTriggerCode = new AtomicInteger(-1);
    private final AtomicInteger lastReceivedCode = new AtomicInteger(-1);

    public IrService(Context pi4j, BoardConfig config, ApplicationEventPublisher eventPublisher,
                     Pi4JService pi4JService, JoystickService joystickService, @Lazy OledService oledService) {
        this.eventPublisher = eventPublisher;
        this.joystickService = joystickService;
        this.oledService = oledService;

        if (!config.hasIrReceiver()) {
            logger.info("IR receiver not available on this board");
            this.irInput = null;
            this.executor = null;
            this.mockMode = false;
            this.available = false;
            return;
        }

        logger.info("Initializing IR receiver on GPIO {}", IR_PIN);

        DigitalInput tempInput = null;
        boolean useMockMode = false;

        try {
            var inputConfig = DigitalInput.newConfigBuilder(pi4j)
                    .id("IR-RECEIVER-BCM" + IR_PIN)
                    .name("IR Receiver")
                    .bcm(IR_PIN)
                    .pull(PullResistance.PULL_UP)
                    .build();
            tempInput = pi4j.create(inputConfig);
            logger.info("IR receiver initialized on GPIO {}", IR_PIN);
        } catch (Exception e) {
            logger.warn("IR receiver initialization failed (running with mock): {}", e.getMessage());
            useMockMode = true;
        }

        this.irInput = tempInput;
        this.mockMode = useMockMode;
        this.available = true;

        // Register the view
        pi4JService.registerView(IrReceiverView.class);

        // IR reading is started on-demand when view is opened (to save CPU)
        logger.info("IR receiver ready - will start reading when view is opened");
    }

    /**
     * Starts the IR reading loop. Called when a view needs IR input.
     * Uses reference counting to support multiple views.
     */
    public synchronized void startReading() {
        if (!available) {
            return;
        }
        int count = activeViewCount.incrementAndGet();
        logger.info("IR startReading called, active views: {}", count);

        if (count == 1 && !running.get()) {
            running.set(true);
            executor = Executors.newSingleThreadExecutor();
            executor.submit(this::irReadLoop);
            updateOledDisplay(-1);
            logger.info("IR reading loop started");
        }
    }

    /**
     * Stops the IR reading loop. Called when view no longer needs IR input.
     * Uses reference counting to support multiple views.
     */
    public synchronized void stopReading() {
        if (!available) {
            return;
        }
        int count = activeViewCount.decrementAndGet();
        logger.info("IR stopReading called, active views: {}", count);

        if (count <= 0) {
            activeViewCount.set(0);
            running.set(false);
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
            logger.info("IR reading loop stopped");
        }
    }

    /**
     * Listens for KEY press events to assign buzzer trigger.
     */
    @EventListener
    public void onKeyStateEvent(KeyStateEvent event) {
        if (!available) {
            return;
        }
        if (event.isPressed()) {
            int lastCode = lastReceivedCode.get();
            if (lastCode >= 0) {
                setBuzzerTriggerCode(lastCode);
                logger.info("KEY pressed - assigned IR code 0x{} as buzzer trigger",
                        Integer.toHexString(lastCode));
                updateOledDisplay(lastCode);
            }
        }
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Sets the IR code that will trigger the buzzer.
     * Set to -1 to disable buzzer triggering.
     */
    public void setBuzzerTriggerCode(int code) {
        buzzerTriggerCode.set(code);
        logger.info("Buzzer trigger code set to: 0x{}", Integer.toHexString(code & 0xFF));
        // Notify listeners of trigger change
        eventPublisher.publishEvent(new IrTriggerChangedEvent(this, code));
    }

    /**
     * Gets the currently configured buzzer trigger code.
     */
    public int getBuzzerTriggerCode() {
        return buzzerTriggerCode.get();
    }

    /**
     * Main IR reading loop - captures pulse timings and decodes.
     */
    private void irReadLoop() {
        logger.info("IR read loop started (sampling every {}us)", SAMPLE_PERIOD_US);

        while (running.get()) {
            try {
                if (mockMode) {
                    Thread.sleep(1000);
                    continue;
                }

                // Wait for start of transmission (falling edge - LOW signal)
                if (irInput.state() == DigitalState.LOW) {
                    // Capture the pulse train
                    List<Integer> pulses = capturePulses();

                    if (pulses.size() >= 10) {
                        logger.info("Captured {} pulses", pulses.size());
                        logPulses(pulses);

                        // Try to decode
                        int code = decodeSignal(pulses);
                        if (code >= 0) {
                            handleIrCode(code);
                        }
                    }

                    // Wait a bit before next capture to avoid re-triggering
                    Thread.sleep(100);
                }

                // Small delay
                Thread.sleep(1);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in IR read loop: {}", e.getMessage());
            }
        }

        logger.info("IR read loop stopped");
    }

    /**
     * Captures pulse timings from the IR receiver.
     * Returns a list of pulse durations in microseconds (alternating LOW/HIGH).
     */
    private List<Integer> capturePulses() {
        List<Integer> pulses = new ArrayList<>();
        DigitalState currentState = DigitalState.LOW;
        int duration = 0;
        int maxPulses = 100;
        int timeout = 0;
        int maxTimeout = 2000; // ~100ms max wait for signal

        while (running.get() && pulses.size() < maxPulses && timeout < maxTimeout) {
            DigitalState state = irInput.state();

            if (state == currentState) {
                duration += SAMPLE_PERIOD_US;
                timeout = 0;
            } else {
                // State changed - record pulse duration
                if (duration > 0) {
                    pulses.add(duration);
                }
                currentState = state;
                duration = SAMPLE_PERIOD_US;
                timeout = 0;
            }

            // Check for end of transmission (long HIGH period)
            if (currentState == DigitalState.HIGH && duration > 10000) {
                break;
            }

            busyWait(SAMPLE_PERIOD_US * 1000L);
            timeout++;
        }

        // Add final pulse
        if (duration > 0 && duration < 10000) {
            pulses.add(duration);
        }

        return pulses;
    }

    /**
     * Logs pulse timings for debugging.
     */
    private void logPulses(List<Integer> pulses) {
        StringBuilder sb = new StringBuilder("Pulses (us): ");
        for (int i = 0; i < Math.min(pulses.size(), 20); i++) {
            sb.append(pulses.get(i));
            if (i < pulses.size() - 1) sb.append(", ");
        }
        if (pulses.size() > 20) {
            sb.append("... (").append(pulses.size()).append(" total)");
        }
        logger.info(sb.toString());
    }

    /**
     * Tries to decode the captured signal using multiple protocols.
     */
    private int decodeSignal(List<Integer> pulses) {
        // Try Sony SIRC first (since user has Sony remote)
        int code = decodeSonySirc(pulses);
        if (code >= 0) {
            logger.info("Decoded Sony SIRC: 0x{}", Integer.toHexString(code));
            return code;
        }

        // Try NEC protocol
        code = decodeNec(pulses);
        if (code >= 0) {
            logger.info("Decoded NEC: 0x{}", Integer.toHexString(code));
            return code;
        }

        // Try raw decode - just return first significant pattern
        code = decodeRaw(pulses);
        if (code >= 0) {
            logger.info("Decoded RAW: 0x{}", Integer.toHexString(code));
            return code;
        }

        return -1;
    }

    /**
     * Decodes Sony SIRC protocol (12, 15, or 20 bit).
     * Start: 2400us pulse, 600us space
     * Bit 0: 600us pulse, 600us space
     * Bit 1: 1200us pulse, 600us space
     */
    private int decodeSonySirc(List<Integer> pulses) {
        if (pulses.size() < 14) return -1; // Need at least start + 12 bits

        // Check start pulse (should be ~2400us)
        int startPulse = pulses.get(0);
        if (startPulse < 1800 || startPulse > 3000) {
            return -1;
        }

        // Check start space (should be ~600us)
        int startSpace = pulses.get(1);
        if (startSpace < 300 || startSpace > 900) {
            return -1;
        }

        // Decode bits
        int code = 0;
        int bitCount = 0;
        for (int i = 2; i < pulses.size() - 1 && bitCount < 20; i += 2) {
            int pulse = pulses.get(i);
            // int space = pulses.get(i + 1); // Space is always ~600us

            // 1200us pulse = 1, 600us pulse = 0
            if (pulse > 900) {
                code |= (1 << bitCount);
            }
            bitCount++;
        }

        if (bitCount >= 7) { // At least 7 bits for command
            // For SIRC, first 7 bits are command, rest is address
            return code & 0x7F; // Return just command
        }

        return -1;
    }

    /**
     * Decodes NEC protocol.
     * Start: 9000us pulse, 4500us space
     * Bit 0: 560us pulse, 560us space
     * Bit 1: 560us pulse, 1690us space
     */
    private int decodeNec(List<Integer> pulses) {
        if (pulses.size() < 66) return -1; // Need start + 32 bits * 2

        // Check start pulse (should be ~9000us)
        int startPulse = pulses.get(0);
        if (startPulse < 7000 || startPulse > 11000) {
            return -1;
        }

        // Check start space (should be ~4500us)
        int startSpace = pulses.get(1);
        if (startSpace < 3500 || startSpace > 5500) {
            return -1;
        }

        // Decode 32 bits
        int[] data = new int[4];
        for (int i = 0; i < 32; i++) {
            int pulseIdx = 2 + i * 2;
            int spaceIdx = pulseIdx + 1;
            if (spaceIdx >= pulses.size()) break;

            int space = pulses.get(spaceIdx);
            int byteIdx = i / 8;
            int bitIdx = i % 8;

            // Long space = 1
            if (space > 1000) {
                data[byteIdx] |= (1 << bitIdx);
            }
        }

        // Validate NEC checksum
        if ((data[0] + data[1]) == 0xFF && (data[2] + data[3]) == 0xFF) {
            return data[2]; // Return command byte
        }

        return -1;
    }

    /**
     * Raw decode - creates a simple hash from the pulse pattern.
     * Useful when protocol is unknown but we want consistent codes.
     */
    private int decodeRaw(List<Integer> pulses) {
        if (pulses.size() < 10) return -1;

        // Create a simple pattern signature
        int code = 0;
        int bitPos = 0;
        int threshold = 800; // Threshold for short/long pulse

        for (int i = 0; i < Math.min(pulses.size(), 16); i++) {
            if (pulses.get(i) > threshold) {
                code |= (1 << bitPos);
            }
            bitPos++;
        }

        return code & 0xFF;
    }

    /**
     * Busy-wait for specified nanoseconds.
     * Checks running flag periodically to allow quick shutdown.
     */
    private void busyWait(long nanos) {
        long start = System.nanoTime();
        int checkCounter = 0;
        while (System.nanoTime() - start < nanos) {
            // Check running flag periodically (not every iteration to maintain timing accuracy)
            if (++checkCounter > 100) {
                checkCounter = 0;
                if (!running.get()) {
                    return;
                }
            }
        }
    }

    /**
     * Handles a successfully decoded IR code.
     */
    private void handleIrCode(int code) {
        logger.info("IR code received: 0x{} (decimal: {}), trigger set to: 0x{}",
                Integer.toHexString(code), code, Integer.toHexString(buzzerTriggerCode.get() & 0xFF));

        // Store as last received code
        lastReceivedCode.set(code);

        // Update OLED display
        updateOledDisplay(code);

        IrCode irCode = new IrCode(code, LocalDateTime.now());
        eventPublisher.publishEvent(new IrCodeEvent(this, irCode));

        // Check if this code should trigger the buzzer
        int trigger = buzzerTriggerCode.get();
        if (trigger >= 0 && code == trigger) {
            logger.info("Buzzer trigger code MATCHED! Calling beep...");
            new Thread(() -> {
                logger.info("Beep thread started, calling joystickService.beep(200)");
                joystickService.beep(200);
                logger.info("Beep thread completed");
            }).start();
        }
    }

    /**
     * Updates the OLED display with IR code information.
     */
    private void updateOledDisplay(int receivedCode) {
        if (oledService == null || !oledService.isAvailable()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("IR Receiver\nLast Code: ");

        if (receivedCode >= 0) {
            sb.append(String.format("0x%02X", receivedCode)).append("\n");
        } else {
            sb.append("--\n");
        }

        int trigger = buzzerTriggerCode.get();
        if (trigger >= 0) {
            sb.append("Buzzer: ").append(String.format("0x%02X", trigger)).append("\n");
        } else {
            sb.append("Buzzer: --\n");
        }

        sb.append("Press KEY to assign");

        oledService.displayText(sb.toString());
    }

    /**
     * Gets the last received IR code.
     */
    public int getLastReceivedCode() {
        return lastReceivedCode.get();
    }

    /**
     * Simulates receiving an IR code (for testing).
     */
    public void simulateIrCode(int code) {
        logger.info("Simulating IR code: 0x{}", Integer.toHexString(code));
        handleIrCode(code);
    }

    /**
     * Shuts down the IR service.
     */
    public void shutdown() {
        running.set(false);
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
