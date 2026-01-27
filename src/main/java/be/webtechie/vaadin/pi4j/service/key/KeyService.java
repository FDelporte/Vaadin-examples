package be.webtechie.vaadin.pi4j.service.key;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.KeyPressView;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.PullResistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for handling key/button input on boards that support it (e.g., Pioneer600).
 */
@Service
public class KeyService {

    private static final Logger logger = LoggerFactory.getLogger(KeyService.class);
    private static final long KEY_DEBOUNCE = 10_000;

    private final DigitalInput key;

    public KeyService(Context pi4j, BoardConfig config, ComponentEventPublisher eventPublisher, Pi4JService pi4JService) {
        if (!config.hasKey() || config.getPinKey() < 0) {
            logger.info("Key sensor not available on this board");
            this.key = null;
            return;
        }

        var keyConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("GPIO-KEY-BCM" + config.getPinKey())
                .name("KeySensor")
                .bcm(config.getPinKey())
                .debounce(KEY_DEBOUNCE)
                .pull(PullResistance.PULL_UP)
                .build();

        this.key = pi4j.create(keyConfig);
        this.key.addListener(e -> {
            logger.info("Key state changed to {}", e.state());
            eventPublisher.publish(ChangeListener.ChangeType.KEY, e.state());
        });

        // Register the view for this feature
        pi4JService.registerView(KeyPressView.class);

        logger.info("Key sensor initialized on pin {}", config.getPinKey());
    }

    /**
     * Returns true if the key service is available on this board.
     */
    public boolean isAvailable() {
        return key != null;
    }
}
