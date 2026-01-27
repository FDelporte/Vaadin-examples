package be.webtechie.vaadin.pi4j.service.sensor;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.TouchView;
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

    private final DigitalInput touch;

    public TouchSensorService(Context pi4j, BoardConfig config, ComponentEventPublisher eventPublisher, Pi4JService pi4JService) {
        if (!config.hasTouch() || config.getPinTouch() < 0) {
            logger.info("Touch sensor not available on this board");
            this.touch = null;
            return;
        }

        var touchConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("GPIO-TOUCH-BCM" + config.getPinTouch())
                .name("TouchSensor")
                .bcm(config.getPinTouch())
                .debounce(TOUCH_DEBOUNCE)
                .pull(PullResistance.PULL_UP)
                .build();

        this.touch = pi4j.create(touchConfig);
        this.touch.addListener(e -> {
            logger.info("Touch state changed to {}", e.state());
            eventPublisher.publish(ChangeListener.ChangeType.TOUCH, e.state());
        });

        // Register the view for this feature
        pi4JService.registerView(TouchView.class);

        logger.info("Touch sensor initialized on pin {}", config.getPinTouch());
    }

    public boolean isAvailable() {
        return touch != null;
    }
}
