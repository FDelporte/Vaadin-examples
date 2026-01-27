package be.webtechie.vaadin.pi4j.event;

import com.pi4j.io.gpio.digital.DigitalState;

/**
 * Event published when the touch sensor state changes.
 */
public class TouchStateEvent extends HardwareEvent {

    private final DigitalState state;

    public TouchStateEvent(Object source, DigitalState state) {
        super(source);
        this.state = state;
    }

    public DigitalState getState() {
        return state;
    }

    public boolean isTouched() {
        return state == DigitalState.HIGH;
    }
}
