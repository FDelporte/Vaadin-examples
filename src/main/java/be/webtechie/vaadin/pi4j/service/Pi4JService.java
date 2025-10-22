package be.webtechie.vaadin.pi4j.service;

import be.webtechie.vaadin.pi4j.service.buzzer.BuzzerComponent;
import be.webtechie.vaadin.pi4j.service.buzzer.PlayNote;
import be.webtechie.vaadin.pi4j.service.matrix.LedMatrixComponent;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixDirection;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixSymbol;
import be.webtechie.vaadin.pi4j.service.rgbmatrix.RgbLedMatrixComponent;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentComponent;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentSymbol;
import com.pi4j.Pi4J;
import com.pi4j.boardinfo.definition.BoardModel;
import com.pi4j.boardinfo.model.BoardInfo;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.character.hd44780.Hd44780Driver;
import com.pi4j.drivers.sensor.environment.bmx280.Bmx280Driver;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.i2c.I2C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class Pi4JService {

    public static final DemoSetupConfig DEMO_SETUP_CONFIG = DemoSetupConfig.CROWPI_2_RPI_5;
    private static final long TOUCH_DEBOUNCE = 10000;
    static Executor executor = Executors.newSingleThreadExecutor();
    private final Context pi4j;
    private final Queue<ChangeListener> listeners;
    private final Logger logger = LoggerFactory.getLogger(Pi4JService.class);
    private final ScheduledExecutorService scheduler;
    private DigitalOutput led;
    private Hd44780Driver lcdDisplay;
    private LedMatrixComponent ledMatrixComponent;
    private RgbLedMatrixComponent rgbLedMatrixComponent;
    private SevenSegmentComponent sevenSegmentComponent;
    private BuzzerComponent buzzerComponent;
    private Bmx280Driver sensorHumidityTemperature;

    public Pi4JService() {
        // This application uses different communication types.
        // As not all Pi4J plugins provide all communication types, we need to explicitly define them.
        // There is also extra configuration needed for the PWM on Raspberry Pi 5.
        pi4j = Pi4J.newAutoContext();

        listeners = new ConcurrentLinkedQueue<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Poller(), 0, 1, TimeUnit.SECONDS);

        initLed();
        initTouch();
        initLcdDisplay();
        initSevenSegment();
        initBuzzer();
        initSensor();

        if (DEMO_SETUP_CONFIG.getHasRGBMatrix()) {
            initRgbLedMatrix();
        } else {
            initLedMatrix();
        }
    }

    private void initLed() {
        try {
            var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                    .id("led")
                    .name("LED")
                    .address(DEMO_SETUP_CONFIG.getPinLed())
                    .shutdown(DigitalState.LOW)
                    .initial(DigitalState.LOW);
            led = pi4j.create(ledConfig);
            logger.info("The LED has been initialized on pin {}", DEMO_SETUP_CONFIG.getPinLed());
        } catch (Exception ex) {
            logger.error("Error while initializing the LED: {}", ex.getMessage());
        }
    }

    private void initTouch() {
        try {
            var touchConfig = DigitalInput.newConfigBuilder(pi4j)
                    .id("BCM" + DEMO_SETUP_CONFIG.getPinTouch())
                    .name("TouchSensor")
                    .address(DEMO_SETUP_CONFIG.getPinTouch())
                    .debounce(TOUCH_DEBOUNCE)
                    .pull(PullResistance.PULL_UP)
                    .build();
            var touch = pi4j.create(touchConfig);
            touch.addListener(e -> {
                logger.info("Touch state changed to {}", e.state());
                broadcast(ChangeListener.ChangeType.TOUCH, e.state());
            });
            logger.info("The touch sensor has been initialized on pin {}", DEMO_SETUP_CONFIG.getPinTouch());
        } catch (Exception ex) {
            logger.error("Error while initializing the touch sensor: {}", ex.getMessage());
        }
    }

    private void initLcdDisplay() {
        try {
            var i2c = pi4j.create(I2C.newConfigBuilder(pi4j)
                    .bus(DEMO_SETUP_CONFIG.getI2cBus())
                    .device(DEMO_SETUP_CONFIG.getI2cDeviceLcd())
                    .build());

            lcdDisplay = Hd44780Driver.withMcp23008Connection(i2c, 16, 2);
            lcdDisplay.writeAt(0, 0, "Hello");
            lcdDisplay.writeAt(0, 1, "   World!");
            logger.info("The LCD display has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the lcd display: {}", ex.getMessage());
        }
    }

    /**
     * Uses a single-color (red) 8x8 LED matrix controlled by the MAX7219 chip via SPI
     */
    private void initLedMatrix() {
        try {
            ledMatrixComponent = new LedMatrixComponent(pi4j);
            ledMatrixComponent.setEnabled(true);
            ledMatrixComponent.setBrightness(7);
            ledMatrixComponent.clear();
            logger.info("The LED matrix has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the LED matrix: {}", ex.getMessage());
        }
    }

    /**
     * Uses a single-color (red) 8x8 LED matrix controlled by the MAX7219 chip via SPI
     */
    private void initRgbLedMatrix() {
        try {
            rgbLedMatrixComponent = new RgbLedMatrixComponent(pi4j, 12);
            logger.info("The RGB LED matrix has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the RGB LED matrix: {}", ex.getMessage());
        }
    }

    private void initSevenSegment() {
        try {
            sevenSegmentComponent = new SevenSegmentComponent(pi4j, DEMO_SETUP_CONFIG.getSevenSegmentDisplayIndexes());
            sevenSegmentComponent.setEnabled(true);
            // Activate full brightness and disable blinking
            // These are the defaults and just here for demonstration purposes
            sevenSegmentComponent.setBlinkRate(0);
            sevenSegmentComponent.setBrightness(15);
            sevenSegmentComponent.clear();
            logger.info("The segment display has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the seven segment component: {}", ex.getMessage());
        }
    }

    private void initBuzzer() {
        try {
            buzzerComponent = new BuzzerComponent(pi4j, DEMO_SETUP_CONFIG.getChannelPwmBuzzer());
            logger.info("The buzzer has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the seven segment component: {}", ex.getMessage());
        }
    }

    private void initSensor() {
        try {

            var i2c = pi4j.create(I2C.newConfigBuilder(pi4j)
                    .bus(DEMO_SETUP_CONFIG.getI2cBus())
                    .device(DEMO_SETUP_CONFIG.getI2cDeviceSensor()));
            sensorHumidityTemperature = new Bmx280Driver(i2c);
            logger.info("The humidity and temperature component has been initialized");
        } catch (Exception ex) {
            logger.error("Error while initializing the humidity and temperature component component: {}", ex.getMessage());
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
            if (DEMO_SETUP_CONFIG.getHasRGBMatrix()) {
                rgbLedMatrixComponent.clear();
                rgbLedMatrixComponent.refresh();
            } else {
                ledMatrixComponent.clear();
                ledMatrixComponent.refresh();
            }
        } catch (Exception ex) {
            logger.error("Can't clear LED matrix: {}", ex.getMessage());
        }
    }

    public void setLedMatrix(MatrixSymbol symbol) {
        logger.info("LED matrix print: {}", symbol.name());
        try {
            if (DEMO_SETUP_CONFIG.getHasRGBMatrix()) {
                rgbLedMatrixComponent.print(symbol, Color.BLUE);
            } else {
                ledMatrixComponent.print(symbol);
            }
        } catch (Exception ex) {
            logger.error("Can't set LED matrix: {}", ex.getMessage());
        }
        broadcast(ChangeListener.ChangeType.MATRIX, "Symbol: " + symbol.name()
                + " - HEX: " + symbol.getHexValue());
    }

    public void moveLedMatrix(MatrixDirection direction) {
        logger.info("LED matrix rotate: {}", direction.name());
        try {
            if (DEMO_SETUP_CONFIG.getHasRGBMatrix()) {
                rgbLedMatrixComponent.rotate(direction);
            } else {
                ledMatrixComponent.rotate(direction);
            }
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
            sevenSegmentComponent.setSymbol(position, symbol);
            sevenSegmentComponent.refresh();
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
            sevenSegmentComponent.clear();
            sevenSegmentComponent.refresh();
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
            String paddedText = String.format("%-16s", text);
            if (paddedText.length() > 16) {
                paddedText = paddedText.substring(0, 16);
            }
            lcdDisplay.writeAt(0, row, paddedText);
        } catch (Exception ex) {
            logger.error("Can't set text on LCD display: {}", ex.getMessage());
        }
        broadcast(ChangeListener.ChangeType.LCD, "Set on row " + row + ": '" + text + "'");
    }

    /**
     * Play a note on the buzzer.
     */
    public void playNote(PlayNote playNote) {
        logger.info("Playing note {}", playNote.note());
        try {
            buzzerComponent.playTone(playNote.note().getFrequency(), playNote.duration());
        } catch (Exception ex) {
            logger.error("Can't play note: {}", ex.getMessage());
        }
        broadcast(ChangeListener.ChangeType.BUZZER, playNote);
    }

    /**
     * Broadcast a change to one of the components to all the listeners.
     **/
    public synchronized <T> void broadcast(ChangeListener.ChangeType type, T message) {
        logger.debug("Broadcast {} to {}", type.name(), message);
        for (ChangeListener listener : listeners) {
            executor.execute(() -> listener.onMessage(type, message));
        }
    }

    /**
     * Poller class which implements {@link Runnable} to be used with {@link ScheduledExecutorService} for repeated execution.
     */
    private final class Poller implements Runnable {
        @Override
        public void run() {
            if (pi4j.boardInfo().getBoardModel() == BoardModel.UNKNOWN) {
                // Generate random measurement
                var temperature = Math.random() * 65 - 20;  // Temperature: -20 to +45°C
                var humidity = Math.random() * 100;         // Humidity: 0 to 100%
                //broadcast(ChangeListener.ChangeType.SENSOR, new Bmx280Driver.Measurement(temperature, humidity, 0F));
            }
            if (sensorHumidityTemperature != null) {
                broadcast(ChangeListener.ChangeType.SENSOR, sensorHumidityTemperature.readMeasurement());
            } else {
                logger.error("No humidity and temperature component found");
            }
        }
    }
}
