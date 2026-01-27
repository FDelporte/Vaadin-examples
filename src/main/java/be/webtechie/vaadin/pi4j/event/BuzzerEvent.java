package be.webtechie.vaadin.pi4j.event;

import be.webtechie.vaadin.pi4j.service.buzzer.PlayNote;

/**
 * Event published when a note is played on the buzzer.
 */
public class BuzzerEvent extends HardwareEvent {

    private final PlayNote playNote;

    public BuzzerEvent(Object source, PlayNote playNote) {
        super(source);
        this.playNote = playNote;
    }

    public PlayNote getPlayNote() {
        return playNote;
    }
}
