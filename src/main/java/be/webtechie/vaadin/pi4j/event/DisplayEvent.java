package be.webtechie.vaadin.pi4j.event;

/**
 * Event published when a display (OLED, LCD, Matrix, Segment) is updated.
 */
public class DisplayEvent extends HardwareEvent {

    private final DisplayType displayType;
    private final String message;

    public DisplayEvent(Object source, DisplayType displayType, String message) {
        super(source);
        this.displayType = displayType;
        this.message = message;
    }

    public DisplayType getDisplayType() {
        return displayType;
    }

    public String getMessage() {
        return message;
    }

    public enum DisplayType {
        OLED,
        LCD,
        MATRIX,
        SEGMENT
    }
}
