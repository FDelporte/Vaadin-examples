package be.webtechie.vaadin.pi4j.event;

import be.webtechie.vaadin.pi4j.service.ir.IrCode;

/**
 * Event published when an IR code is received.
 */
public class IrCodeEvent extends HardwareEvent {

    private final IrCode irCode;

    public IrCodeEvent(Object source, IrCode irCode) {
        super(source);
        this.irCode = irCode;
    }

    public IrCode getIrCode() {
        return irCode;
    }

    public int getCode() {
        return irCode.code();
    }

    public String getCodeHex() {
        return irCode.getCodeHex();
    }
}
