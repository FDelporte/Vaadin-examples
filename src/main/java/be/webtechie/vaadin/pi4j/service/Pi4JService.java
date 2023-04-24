package be.webtechie.vaadin.pi4j.service;

import com.pi4j.Pi4J;
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

    private static final int PIN_BUTTON = 24; // PIN 18 = BCM 24
    private static final int PIN_LED = 22; // PIN 15 = BCM 22
    private final Context pi4j;
    private final Queue<ButtonListener> buttonListeners;
    Logger logger = LoggerFactory.getLogger(Pi4JService.class);
    private DigitalOutput led;

    public Pi4JService() {
        pi4j = Pi4J.newAutoContext();
        buttonListeners = new ConcurrentLinkedQueue<>();
        initLed();
        initButton();
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

    private void initButton() {
        try {
            var buttonConfig = DigitalInput.newConfigBuilder(pi4j)
                    .id("button")
                    .name("Button")
                    .address(PIN_BUTTON)
                    .pull(PullResistance.PULL_DOWN)
                    .debounce(3000L)
                    .provider("pigpio-digital-input");
            var button = pi4j.create(buttonConfig);
            button.addListener(e -> {
                logger.info("Button state changed to {}", e.state());
                buttonListeners.forEach(bl -> bl.onButtonEvent(e.state()));
            });
            logger.info("The button has been initialized on pin {}", PIN_BUTTON);
        } catch (Exception ex) {
            logger.error("Error while initializing the button: {}", ex.getMessage());
        }
    }

    /**
     * Add a button listener which will get all state changes of the button DigitalInput
     *
     * @param buttonListener
     */
    public void addButtonListener(ButtonListener buttonListener) {
        buttonListeners.add(buttonListener);
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
                .collect(Collectors.joining(","));
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
                .collect(Collectors.joining(","));
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
                .collect(Collectors.joining(","));
    }
}
