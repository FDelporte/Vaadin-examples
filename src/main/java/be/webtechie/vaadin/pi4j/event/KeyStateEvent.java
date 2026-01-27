package be.webtechie.vaadin.pi4j.event;

import com.pi4j.io.gpio.digital.DigitalState;

/**
 * Event published when the key/button state changes.
 */
public class KeyStateEvent extends HardwareEvent {

    private final DigitalState state;

    public KeyStateEvent(Object source, DigitalState state) {
        super(source);
        this.state = state;
    }

    public DigitalState getState() {
        return state;
    }

    public boolean isPressed() {
        return state == DigitalState.LOW;
    }
}
