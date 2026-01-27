package be.webtechie.vaadin.pi4j.event;

import be.webtechie.vaadin.pi4j.service.joystick.JoystickDirection;

/**
 * Event published when the joystick direction changes.
 */
public class JoystickEvent extends HardwareEvent {

    private final JoystickDirection direction;

    public JoystickEvent(Object source, JoystickDirection direction) {
        super(source);
        this.direction = direction;
    }

    public JoystickDirection getDirection() {
        return direction;
    }
}
