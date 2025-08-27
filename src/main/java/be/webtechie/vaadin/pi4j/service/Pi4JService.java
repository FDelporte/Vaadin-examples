package be.webtechie.vaadin.pi4j.service;

import be.webtechie.vaadin.pi4j.service.buzzer.BuzzerComponent;
import be.webtechie.vaadin.pi4j.service.buzzer.Note;
import be.webtechie.vaadin.pi4j.service.lcd.LcdDisplayComponent;
import be.webtechie.vaadin.pi4j.service.matrix.LedMatrixComponent;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixDirection;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixSymbol;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentComponent;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentSymbol;
import com.pi4j.Pi4J;
import com.pi4j.boardinfo.model.BoardInfo;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class Pi4JService {

    private static final CrowPiConfig crowPiConfig = CrowPiConfig.CROWPI_2;
    private static final long TOUCH_DEBOUNCE = 10000;
    static Executor executor = Executors.newSingleThreadExecutor();
    private final Context pi4j;
    private final Queue<ChangeListener> listeners;
    private final Logger logger = LoggerFactory.getLogger(Pi4JService.class);
    private DigitalOutput led;
    private LcdDisplayComponent lcdDisplay;
    private LedMatrixComponent ledMatrix;
    private SevenSegmentComponent sevenSegment;
    private BuzzerComponent buzzer;

    public Pi4JService() {
        pi4j = Pi4J.newAutoContext();
        listeners = new ConcurrentLinkedQueue<>();
        initLed();
        initTouch();
        initLcdDisplay();
        initLedMatrix();
        initSevenSegment();
        initBuzzer();
    }

    private void initLed() {
        try {
            var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                    .id("led")
                    .name("LED")
                    .address(crowPiConfig.getPinLed())
                    .shutdown(DigitalState.LOW)
                    .initial(DigitalState.LOW);
            led = pi4j.create(ledConfig);
            logger.info("The LED has been initialized on pin {}", crowPiConfig.getPinLed());
        } catch (Exception ex) {
            logger.error("Error while initializing the LED: {}", ex.getMessage());
        }
    }

    private void initTouch() {
        try {
            var touchConfig = DigitalInput.newConfigBuilder(pi4j)
                    .id("BCM" + crowPiConfig.getPinTouch())
                    .name("TouchSensor")
                    .address(crowPiConfig.getPinTouch())
                    .debounce(TOUCH_DEBOUNCE)
                    .pull(PullResistance.PULL_UP)
                    .build();
            var touch = pi4j.create(touchConfig);
            touch.addListener(e -> {
                logger.info("Touch state changed to {}", e.state());
                broadcast(ChangeListener.ChangeType.TOUCH, String.valueOf(e.state().value()));
            });
            logger.info("The touch sensor has been initialized on pin {}", crowPiConfig.getPinTouch());
        } catch (Exception ex) {
            logger.error("Error while initializing the touch sensor: {}", ex.getMessage());
        }
    }

    private void initLcdDisplay() {
        try {
            lcdDisplay = new LcdDisplayComponent(pi4j);
            lcdDisplay.initialize();
            lcdDisplay.writeLine("Hello", 1);
            lcdDisplay.writeLine("   World!", 2);
            logger.info("The LCD display has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the lcd display: {}", ex.getMessage());
        }
    }

    private void initLedMatrix() {
        try {
            ledMatrix = new LedMatrixComponent(pi4j);
            ledMatrix.setEnabled(true);
            ledMatrix.setBrightness(7);
            ledMatrix.clear();
            logger.info("The LED matrix has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the LED matrix: {}", ex.getMessage());
        }
    }

    private void initSevenSegment() {
        try {
            sevenSegment = new SevenSegmentComponent(pi4j);
            sevenSegment.setEnabled(true);
            // Activate full brightness and disable blinking
            // These are the defaults and just here for demonstration purposes
            sevenSegment.setBlinkRate(0);
            sevenSegment.setBrightness(15);
            sevenSegment.clear();
            logger.info("The segment display has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the seven segment component: {}", ex.getMessage());
        }
    }

    private void initBuzzer() {
        try {
            buzzer = new BuzzerComponent(pi4j);
            logger.info("The buzzer has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the seven segment component: {}", ex.getMessage());
        }
    }

    /**
     * Add a change listener which will get all state changes of a component
     */
    public synchronized void addListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a change listener
     */
    public synchronized void removeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Return the info about the detected board
     */
    public BoardInfo getBoardInfo() {
        return pi4j.boardInfo();
    }

    /**
     * Toggle the LED on or off.
     */
    public void setLedState(boolean on) {
        if (led == null) {
            return;
        }
        led.setState(on);
    }

    /**
     * A single 'default' platform is auto-assigned during Pi4J initialization based on a weighting value provided
     * by each platform implementation at runtime. Additionally, you can override this behavior and assign your own
     * 'default' platform anytime after initialization.
     * The default platform is a single platform instance from the managed platforms collection that will serve to
     * define the default I/O providers that Pi4J will use for each given I/O interface when creating and registering
     * I/O instances.
     */
    public String getDefaultPlatform() {
        if (pi4j == null || pi4j.platform() == null) {
            return "None";
        }
        return pi4j.platform().name();
    }

    /**
     * Platforms represent the physical layout of a system's hardware I/O
     * capabilities and what I/O providers the target platform supports.  For example, a 'RaspberryPi' platform supports
     * `Digital` inputs and outputs, PWM, I2C, SPI, and Serial but does not support a default provider for 'Analog'
     * inputs and outputs.
     * Platforms also provide validation for the I/O pins and their capabilities for the target hardware.
     */
    public String getLoadedPlatforms() {
        if (pi4j == null || pi4j.platforms() == null) {
            return "None";
        }
        return pi4j.platforms().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
    }

    /**
     * Providers are intended to represent I/O implementations and provide access to the I/O interfaces available on
     * the system. Providers 'provide' concrete runtime implementations of I/O interfaces.
     */
    public String getProviders() {
        if (pi4j == null || pi4j.providers() == null) {
            return "None";
        }
        return pi4j.providers().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
    }

    /**
     * The registry stores the state of all the I/O managed by Pi4J.
     */
    public String getRegistry() {
        if (pi4j == null || pi4j.registry() == null) {
            return "None";
        }
        return pi4j.registry().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
    }

    public void clearLedMatrix() {
        logger.info("Clearing LED matrix");
        try {
            ledMatrix.clear();
            ledMatrix.refresh();
        } catch (Exception ex) {
            logger.error("Can't clear LED matrix: {}", ex.getMessage());
        }
    }

    public void setLedMatrix(MatrixSymbol symbol) {
        logger.info("LED matrix print: {}", symbol.name());
        try {
            ledMatrix.print(symbol);
        } catch (Exception ex) {
            logger.error("Can't set LED matrix: {}", ex.getMessage());
        }
        broadcast(ChangeListener.ChangeType.MATRIX, "Symbol: " + symbol.name()
                + " - HEX: " + symbol.getHexValue());
    }

    public void moveLedMatrix(MatrixDirection direction) {
        logger.info("LED matrix rotate: {}", direction.name());
        try {
            ledMatrix.rotate(direction);
        } catch (Exception ex) {
            logger.error("Can't move LED matrix: {}", ex.getMessage());
        }
        broadcast(ChangeListener.ChangeType.MATRIX, "Move: " + direction.name());
    }

    /**
     * Set a symbol on one of the seven segment display.
     */
    public void setSevenSegment(int position, SevenSegmentSymbol symbol) {
        logger.info("Setting digit {} on position {} of seven segment display", symbol.name(), position);
        try {
            sevenSegment.setSymbol(position, symbol);
            sevenSegment.refresh();
        } catch (Exception ex) {
            logger.error("Can't set seven segment: {}", ex.getMessage());
        }
        broadcast(ChangeListener.ChangeType.SEGMENT, "Position: " + (position + 1)
                + " - Symbol: " + symbol.name()
                + " - HEX: " + symbol.getHexValue()
                + " - Bits: " + symbol.getBitsValue());
    }

    /**
     * Clear the seven segment display.
     */
    public void clearSevenSegment() {
        logger.info("Clearing seven segment display");
        try {
            sevenSegment.clear();
            sevenSegment.refresh();
        } catch (Exception ex) {
            logger.error("Can't clear seven segment: {}", ex.getMessage());
        }
    }

    /**
     * Clear the LCD display.
     */
    public void clearLcdDisplay() {
        logger.info("Clearing LCD display");
        try {
            lcdDisplay.clearDisplay();
        } catch (Exception ex) {
            logger.error("Can't clear seven segment: {}", ex.getMessage());
        }
    }

    /**
     * Set a text to one of the rows of the LCD display.
     */
    public void setLcdDisplay(int row, String text) {
        logger.info("Setting LCD display line {} to '{}'", row, text);
        try {
            lcdDisplay.writeLine(text, row);
        } catch (Exception ex) {
            logger.error("Can't set text on LCD display: {}", ex.getMessage());
        }
        broadcast(ChangeListener.ChangeType.LCD, "Set on row " + row + ": '" + text + "'");
    }

    /**
     * Play a note on the buzzer.
     */
    public void playNote(Note note) {
        logger.info("Playing note {}", note);
        try {
            buzzer.playTone(note.getFrequency(), 150);
        } catch (Exception ex) {
            logger.error("Can't play note: {}", ex.getMessage());
        }
        broadcast(ChangeListener.ChangeType.BUZZER, "Played note " + note.name()
                + " - Frequency: " + note.getFrequency()
                + " - HEX: " + note.getHexValue());
    }

    /**
     * Broadcast a change to one of the components to all the listeners.
     **/
    public synchronized void broadcast(ChangeListener.ChangeType type, String message) {
        logger.debug("Broadcast {} to {}", type.name(), message);
        for (ChangeListener listener : listeners) {
            executor.execute(() -> {
                listener.onMessage(type, message);
            });
        }
    }
}
