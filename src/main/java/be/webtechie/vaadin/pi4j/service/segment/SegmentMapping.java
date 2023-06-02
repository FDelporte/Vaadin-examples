package be.webtechie.vaadin.pi4j.service.segment;

public enum SegmentMapping {
    TOP(0),
    RIGHT_TOP(1),
    RIGHT_BOTTOM(2),
    BOTTOM(3),
    LEFT_BOTTOM(4),
    LEFT_TOP(5),
    CENTER(6),
    DECIMAL_POINT(7);

    private final byte value;

    SegmentMapping(int bit) {
        this.value = (byte) (1 << bit);
    }

    byte getValue() {
        return this.value;
    }
}
