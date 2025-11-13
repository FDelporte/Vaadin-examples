package be.webtechie.vaadin.pi4j.service.led;

import be.webtechie.vaadin.pi4j.config.CrowPiConfig;
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

    public LedService(Context pi4j, CrowPiConfig config) {
        var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("GPIO-LED-BCM" + config.getPinLed())
                .name("LED")
                .bcm(config.getPinLed())
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW);
        this.led = pi4j.create(ledConfig);
        logger.info("LED initialized on pin {}", config.getPinLed());
    }

    public boolean getState() {
        return led.state() == DigitalState.HIGH;
    }

    public void setState(boolean on) {
        led.setState(on);
    }
}