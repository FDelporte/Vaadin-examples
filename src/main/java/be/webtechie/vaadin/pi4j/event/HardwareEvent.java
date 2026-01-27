package be.webtechie.vaadin.pi4j.event;

import org.springframework.context.ApplicationEvent;

/**
 * Base class for all hardware-related events from Pi4J components.
 */
public abstract class HardwareEvent extends ApplicationEvent {

    protected HardwareEvent(Object source) {
        super(source);
    }
}
