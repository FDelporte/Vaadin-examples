package be.webtechie.vaadin.pi4j.service;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.util.Console;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Pi4JService {

    private final Context pi4j;
    private final Console console;

    private static final int PIN_BUTTON = 24; // PIN 18 = BCM 24
    private static final int PIN_LED = 22; // PIN 15 = BCM 22

    private final List<ButtonListener> buttonListeners;
    private DigitalOutput led;
    private DigitalInput button;

    public Pi4JService() {
        pi4j = Pi4J.newAutoContext();
        console = new Console();

        buttonListeners = new ArrayList<>();

        initLed();
        initButton();
    }

    private void initLed() {
        try {
            var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                    .id("led")
                    .name("LED Flasher")
                    .address(PIN_LED)
                    .shutdown(DigitalState.LOW)
                    .initial(DigitalState.LOW)
                    .provider("pigpio-digital-output");
            led = pi4j.create(ledConfig);
        } catch (Exception ex) {
            console.println("Error while initializing the LED: " + ex.getMessage());
        }
    }

    private void initButton() {
        try {
            var buttonConfig = DigitalInput.newConfigBuilder(pi4j)
                    .id("button")
                    .name("Press button")
                    .address(PIN_BUTTON)
                    .pull(PullResistance.PULL_DOWN)
                    .debounce(3000L)
                    .provider("pigpio-digital-input");
            button = pi4j.create(buttonConfig);
            button.addListener(e -> buttonListeners.forEach(bl -> bl.onButtonEvent(e.state())));
        } catch (Exception ex) {
            console.println("Error while initializing the button: " + ex.getMessage());
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
        return pi4j.platform().describe().description();
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
        return pi4j.platforms().describe().description();
    }

    /**
     * Providers are intended to represent I/O implementations and provide access to the I/O interfaces available on
     * the system. Providers 'provide' concrete runtime implementations of I/O interfaces.
     */
    public String getProviders() {
        if (pi4j == null || pi4j.providers() == null) {
            return "None";
        }
        return pi4j.providers().describe().description();
    }

    /**
     * The registry stores the state of all the I/O managed by Pi4J.
     */
    public String getRegistry() {
        if (pi4j == null || pi4j.registry() == null) {
            return "None";
        }
        return pi4j.registry().describe().description();
    }

    /**
     * Toggle the LED on or off.
     *
     * @param on
     */
    public void setLedState(boolean on) {
        led.setState(on);
    }
}
