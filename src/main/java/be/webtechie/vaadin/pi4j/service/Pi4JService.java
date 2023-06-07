package be.webtechie.vaadin.pi4j.service;

import be.webtechie.vaadin.pi4j.service.matrix.LedMatrixComponent;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixDirection;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixListener;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixSymbol;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentComponent;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentListener;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentSymbol;
import be.webtechie.vaadin.pi4j.service.touch.TouchListener;
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
import java.util.stream.Collectors;

@Service
public class Pi4JService {

    private static final int PIN_LED = 22;
    private static final int PIN_TOUCH = 17;
    private static final long TOUCH_DEBOUNCE = 10000;
    private final Context pi4j;
    private final Queue<TouchListener> touchListeners;
    private final Queue<MatrixListener> matrixListeners;
    private final Queue<SevenSegmentListener> sevenSegmentListeners;
    private final Logger logger = LoggerFactory.getLogger(Pi4JService.class);
    private DigitalOutput led;
    private LedMatrixComponent ledMatrix;
    private SevenSegmentComponent sevenSegment;
    private MatrixSymbol currentSymbol = MatrixSymbol.EMPTY;
    private MatrixDirection currentDirection = MatrixDirection.UP;

    public Pi4JService() {
        pi4j = CrowPiPlatform.buildNewContext();
        touchListeners = new ConcurrentLinkedQueue<>();
        matrixListeners = new ConcurrentLinkedQueue<>();
        sevenSegmentListeners = new ConcurrentLinkedQueue<>();
        initLed();
        initTouch();
        initLedMatrix();
        initSevenSegment();
    }

    private void initLed() {
        try {
            var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                    .id("led")
                    .name("LED")
                    .address(PIN_LED)
                    .shutdown(DigitalState.LOW)
                    .initial(DigitalState.LOW)
                    .provider("pigpio-digital-output");
            led = pi4j.create(ledConfig);
            logger.info("The LED has been initialized on pin {}", PIN_LED);
        } catch (Exception ex) {
            logger.error("Error while initializing the LED: {}", ex.getMessage());
        }
    }

    private void initTouch() {
        try {
            var touchConfig = DigitalInput.newConfigBuilder(pi4j)
                    .id("BCM" + PIN_TOUCH)
                    .name("TouchSensor")
                    .address(PIN_TOUCH)
                    .debounce(TOUCH_DEBOUNCE)
                    .pull(PullResistance.PULL_UP)
                    .build();
            var touch = pi4j.create(touchConfig);
            touch.addListener(e -> {
                logger.info("Touch state changed to {}", e.state());
                touchListeners.forEach(bl -> bl.onTouchEvent(e.state()));
            });
            logger.info("The touch sensor has been initialized on pin {}", PIN_TOUCH);
        } catch (Exception ex) {
            logger.error("Error while initializing the touch sensor: {}", ex.getMessage());
        }
    }

    private void initLedMatrix() {
        try {
            ledMatrix = new LedMatrixComponent(pi4j);
            ledMatrix.setEnabled(true);
            ledMatrix.setBrightness(7);
            setLedMatrix(currentSymbol);
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
        } catch (Exception ex) {
            logger.error("Error while initializing the seven segment component: {}", ex.getMessage());
        }
    }

    /**
     * Add a button listener which will get all state changes of the button DigitalInput
     *
     * @param listener
     */
    public void addButtonListener(TouchListener listener) {
        touchListeners.add(listener);
    }

    public void removeButtonListener(TouchListener listener) {
        touchListeners.remove(listener);
    }

    /**
     * Add a matrix listener which will get all matrix changes of the LedMatrix
     *
     * @param listener
     */
    public void addMatrixListener(MatrixListener listener) {
        matrixListeners.add(listener);
    }

    public void removeMatrixListener(MatrixListener listener) {
        matrixListeners.remove(listener);
    }

    /**
     * Add a seven segment listener which will get all seven segment changes of the SevenSegmentDisplay
     *
     * @param listener
     */
    public void addSevenSegmentListener(SevenSegmentListener listener) {
        sevenSegmentListeners.add(listener);
    }

    public void removeSevenSegmentListener(SevenSegmentListener listener) {
        sevenSegmentListeners.remove(listener);
    }

    /**
     * Toggle the LED on or off.
     *
     * @param on
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
        currentSymbol = symbol;
        notifyMatrixListeners();
    }

    public void moveLedMatrix(MatrixDirection direction) {
        logger.info("LED matrix rotate: {}", direction.name());
        try {
            ledMatrix.rotate(direction);
        } catch (Exception ex) {
            logger.error("Can't move LED matrix: {}", ex.getMessage());
        }
        currentDirection = direction;
        notifyMatrixListeners();
    }

    private void notifyMatrixListeners() {
        matrixListeners.forEach(ml -> ml.onMatrixChange(currentSymbol, currentDirection));
    }

    /**
     * Set a symbol on one of the seven segment display.
     *
     * @param position
     * @param symbol
     */
    public void setSevenSegment(int position, SevenSegmentSymbol symbol) {
        logger.info("Setting digit {} on position {} of seven segment display", symbol.name(), position);
        try {
            sevenSegment.setSymbol(position, symbol);
            sevenSegment.refresh();
        } catch (Exception ex) {
            logger.error("Can't set seven segment: {}", ex.getMessage());
        }
        sevenSegmentListeners.forEach(ssl -> ssl.onSevenSegmentChange(position, symbol));
    }

    public void clearSevenSegment() {
        logger.info("Clearing seven segment display");
        try {
            sevenSegment.clear();
            sevenSegment.refresh();
        } catch (Exception ex) {
            logger.error("Can't clear seven segment: {}", ex.getMessage());
        }
    }
}
