package be.webtechie.vaadin.pi4j.service.buzzer;

import be.webtechie.vaadin.pi4j.config.CrowPiConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BuzzerService {

    private static final Logger logger = LoggerFactory.getLogger(BuzzerService.class);
    private final BuzzerComponent buzzerComponent;
    private final ComponentEventPublisher eventPublisher;

    public BuzzerService(Context pi4j, CrowPiConfig config, ComponentEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.buzzerComponent = new BuzzerComponent(pi4j, config.getChannelPwmBuzzer());
        logger.info("Buzzer initialized");
    }

    public void playNote(PlayNote playNote) {
        logger.info("Playing note {}", playNote.note());
        buzzerComponent.playTone(playNote.note().getFrequency(), playNote.duration());
        eventPublisher.publish(ChangeListener.ChangeType.BUZZER, playNote);
    }
}