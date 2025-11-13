package be.webtechie.vaadin.pi4j.service.sensor;

import be.webtechie.vaadin.pi4j.config.CrowPiConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.PullResistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TouchSensorService {

    private static final Logger logger = LoggerFactory.getLogger(TouchSensorService.class);
    private static final long TOUCH_DEBOUNCE = 10_000;

    public TouchSensorService(Context pi4j, CrowPiConfig config, ComponentEventPublisher eventPublisher) {
        var touchConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("GPIO-TOUCH-BCM" + config.getPinTouch())
                .name("TouchSensor")
                .bcm(config.getPinTouch())
                .debounce(TOUCH_DEBOUNCE)
                .pull(PullResistance.PULL_UP)
                .build();

        var touch = pi4j.create(touchConfig);
        touch.addListener(e -> {
            logger.info("Touch state changed to {}", e.state());
            eventPublisher.publish(ChangeListener.ChangeType.TOUCH, e.state());
        });

        logger.info("Touch sensor initialized on pin {}", config.getPinTouch());
    }
}