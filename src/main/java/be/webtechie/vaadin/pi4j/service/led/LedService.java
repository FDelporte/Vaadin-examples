package be.webtechie.vaadin.pi4j.service.led;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.LEDView;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LedService {

    private static final Logger logger = LoggerFactory.getLogger(LedService.class);
    private final DigitalOutput led;

    public LedService(Context pi4j, BoardConfig config, Pi4JService pi4JService) {
        if (!config.hasLed() || config.getPinLed() < 0) {
            logger.info("LED not available on this board");
            this.led = null;
            return;
        }

        var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("GPIO-LED-BCM" + config.getPinLed())
                .name("LED")
                .bcm(config.getPinLed())
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW);
        this.led = pi4j.create(ledConfig);

        // Register the view for this feature
        pi4JService.registerView(LEDView.class);

        logger.info("LED initialized on pin {}", config.getPinLed());
    }

    public boolean isAvailable() {
        return led != null;
    }

    public boolean getState() {
        if (led == null) {
            return false;
        }
        return led.state() == DigitalState.HIGH;
    }

    public void setState(boolean on) {
        if (led == null) {
            logger.warn("LED not available");
            return;
        }
        led.setState(on);
    }
}
