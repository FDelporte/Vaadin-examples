package be.webtechie.vaadin.pi4j.service.ir;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a received IR code.
 */
public record IrCode(int code, LocalDateTime timestamp) {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public String getCodeHex() {
        return String.format("0x%02X", code);
    }

    public String getTimestampFormatted() {
        return timestamp.format(TIME_FORMAT);
    }

    @Override
    public String toString() {
        return getTimestampFormatted() + " - " + getCodeHex();
    }
}
