package be.webtechie.vaadin.pi4j.service.ir;

/**
 * Event indicating that the IR buzzer trigger code has changed.
 */
public record IrTriggerChanged(int triggerCode) {

    public String getTriggerCodeHex() {
        return triggerCode >= 0 ? String.format("0x%02X", triggerCode) : "";
    }
}
