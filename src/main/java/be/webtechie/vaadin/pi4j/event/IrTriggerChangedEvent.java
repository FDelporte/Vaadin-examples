package be.webtechie.vaadin.pi4j.event;

/**
 * Event published when the IR buzzer trigger code is changed.
 */
public class IrTriggerChangedEvent extends HardwareEvent {

    private final int triggerCode;

    public IrTriggerChangedEvent(Object source, int triggerCode) {
        super(source);
        this.triggerCode = triggerCode;
    }

    public int getTriggerCode() {
        return triggerCode;
    }

    public String getTriggerCodeHex() {
        return triggerCode >= 0 ? String.format("0x%02X", triggerCode) : "";
    }
}
