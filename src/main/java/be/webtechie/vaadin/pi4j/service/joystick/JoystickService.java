package be.webtechie.vaadin.pi4j.service.joystick;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.JoystickView;
import be.webtechie.vaadin.pi4j.views.electronics.SimpleBuzzerView;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for reading joystick input via PCF8574 I/O expander.
 * Publishes joystick direction changes via ComponentEventPublisher.
 */
@Service
public class JoystickService {

    private static final Logger logger = LoggerFactory.getLogger(JoystickService.class);
    private static final long POLLING_INTERVAL_MS = 100; // Poll every 100ms

    private final PCF8574 pcf8574;
    private final ComponentEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;
    private final boolean mockMode;
    private final Random random = new Random();

    private JoystickDirection lastDirection = JoystickDirection.NONE;

    public JoystickService(Context pi4j, BoardConfig config, ComponentEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;

        if (!config.hasJoystick() || config.getI2cDevicePcf8574() == 0x00) {
            logger.info("Joystick not available on this board");
            this.pcf8574 = null;
            this.scheduler = null;
            this.mockMode = false;
            return;
        }

        logger.info("Initializing joystick via PCF8574 on I2C bus {} at address 0x{}",
                config.getI2cBus(), Integer.toHexString(config.getI2cDevicePcf8574() & 0xFF));

        PCF8574 tempPcf8574 = null;
        boolean useMockMode = false;

        try {
            tempPcf8574 = new PCF8574(pi4j, config.getI2cBus(), config.getI2cDevicePcf8574() & 0xFF);
            // Test read to verify communication
            var testDirection = tempPcf8574.readJoystick();
            logger.info("Joystick initialized successfully, test read: {}", testDirection);
        } catch (Exception e) {
            logger.error("PCF8574 initialization FAILED - running in mock mode. Error: {}", e.getMessage(), e);
            tempPcf8574 = null;
            useMockMode = true;
        }

        this.pcf8574 = tempPcf8574;
        this.mockMode = useMockMode;

        // Register the views
        pi4JService.registerView(JoystickView.class);
        if (config.hasPcf8574Buzzer()) {
            pi4JService.registerView(SimpleBuzzerView.class);
        }

        // Start polling
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::pollJoystick, 0, POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns true if the joystick is available.
     */
    public boolean isAvailable() {
        return pcf8574 != null || mockMode;
    }

    /**
     * Reads the current joystick direction.
     */
    public JoystickDirection readDirection() {
        if (pcf8574 == null) {
            return JoystickDirection.NONE;
        }
        return pcf8574.readJoystick();
    }

    /**
     * Triggers a short beep using the PCF8574 buzzer.
     *
     * @param durationMs duration of the beep in milliseconds
     */
    public void beep(int durationMs) {
        logger.info("beep({}) called, pcf8574={}", durationMs, pcf8574 != null ? "initialized" : "null");
        if (pcf8574 == null) {
            logger.debug("Beep requested (mock mode, duration: {}ms)", durationMs);
            return;
        }
        try {
            pcf8574.setBeeper(true);
            Thread.sleep(durationMs);
            pcf8574.setBeeper(false);
            logger.info("beep({}) completed", durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pcf8574.setBeeper(false);
        }
    }

    /**
     * Triggers a short beep (100ms).
     */
    public void beep() {
        beep(100);
    }

    private void pollJoystick() {
        try {
            JoystickDirection direction;
            if (mockMode) {
                // In mock mode, occasionally generate random directions for testing
                if (random.nextInt(50) == 0) {
                    direction = JoystickDirection.values()[random.nextInt(JoystickDirection.values().length)];
                } else {
                    direction = JoystickDirection.NONE;
                }
            } else {
                direction = pcf8574.readJoystick();
            }

            // Only publish if direction changed
            if (direction != lastDirection) {
                lastDirection = direction;
                logger.debug("Joystick direction: {}", direction);
                eventPublisher.publish(ChangeListener.ChangeType.JOYSTICK, direction);
            }
        } catch (Exception e) {
            logger.error("Error reading joystick: {}", e.getMessage());
        }
    }
}
