package be.webtechie.vaadin.pi4j.service.buzzer;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.BuzzerEvent;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.SleepHelper;
import be.webtechie.vaadin.pi4j.views.electronics.BuzzerView;
import com.pi4j.context.Context;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class BuzzerService {

    private static final Logger logger = LoggerFactory.getLogger(BuzzerService.class);
    private final ApplicationEventPublisher eventPublisher;
    private Pwm pwm;

    public BuzzerService(Context pi4j, BoardConfig config, ApplicationEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;

        if (!config.hasBuzzer()) {
            logger.info("Buzzer not available on this board");
            return;
        }

        try {
            var pwmConfig = Pwm.newConfigBuilder(pi4j)
                    .pwmType(PwmType.HARDWARE)
                    .chip(config.getPwmChip())
                    .channel(config.getPwmChannelBuzzer())
                    .initial(0)
                    .shutdown(0)
                    .build();
            this.pwm = pi4j.create(pwmConfig);

            // Register the view for this feature
            pi4JService.registerView(BuzzerView.class);

            logger.info("Buzzer initialized");
        } catch (Exception e) {
            logger.error("Error initializing buzzer: {}", e.getMessage());
        }
    }

    public boolean isAvailable() {
        return pwm != null;
    }

    public void playNote(PlayNote playNote) {
        if (this.pwm == null) {
            logger.error("Buzzer not initialized");
            return;
        }
        logger.info("Playing note {}", playNote.note());
        playTone(playNote.note().getFrequency(), playNote.duration());
        eventPublisher.publishEvent(new BuzzerEvent(this, playNote));
    }

    /**
     * Plays a tone with the given frequency in Hz for a specific duration.
     * This method is blocking and will sleep until the specified duration has passed.
     * A frequency of zero causes the buzzer to play silence.
     * A duration of zero to play the tone indefinitely and return immediately.
     *
     * @param frequency Frequency in Hz
     * @param duration  Duration in milliseconds
     */
    private void playTone(int frequency, int duration) {
        if (frequency > 0) {
            // Activate the PWM with a duty cycle of 50% and the given frequency in Hz.
            // This causes the buzzer to be on for half of the time during each cycle, resulting in the desired frequency.
            pwm.on(50, frequency);

            // If the duration is larger than zero, the tone should be automatically stopped after the given duration.
            if (duration > 0) {
                SleepHelper.sleep(duration);
                this.playSilence();
            }
        } else {
            this.playSilence(duration);
        }
    }

    /**
     * Silences the buzzer and returns immediately.
     */
    private void playSilence() {
        pwm.off();
    }

    /**
     * Silences the buzzer and waits for the given duration.
     * This method is blocking and will sleep until the specified duration has passed.
     *
     * @param duration Duration in milliseconds
     */
    private void playSilence(int duration) {
        this.playSilence();
        SleepHelper.sleep(duration);
    }
}
