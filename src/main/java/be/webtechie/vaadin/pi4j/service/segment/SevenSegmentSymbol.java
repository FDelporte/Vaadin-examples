package be.webtechie.vaadin.pi4j.service.segment;

public enum SevenSegmentSymbol {
    EMPTY("EMPTY", fromSegments()),
    MINUS("-", fromSegments(SegmentMapping.CENTER)),
    NUMBER_0("0", fromSegments(SegmentMapping.TOP, SegmentMapping.LEFT_TOP, SegmentMapping.RIGHT_TOP, SegmentMapping.LEFT_BOTTOM, SegmentMapping.RIGHT_BOTTOM, SegmentMapping.BOTTOM)),
    NUMBER_1("1", fromSegments(SegmentMapping.RIGHT_TOP, SegmentMapping.RIGHT_BOTTOM)),
    NUMBER_2("2", fromSegments(SegmentMapping.TOP, SegmentMapping.RIGHT_TOP, SegmentMapping.CENTER, SegmentMapping.LEFT_BOTTOM, SegmentMapping.BOTTOM)),
    NUMBER_3("3", fromSegments(SegmentMapping.TOP, SegmentMapping.RIGHT_TOP, SegmentMapping.CENTER, SegmentMapping.RIGHT_BOTTOM, SegmentMapping.BOTTOM)),
    NUMBER_4("4", fromSegments(SegmentMapping.LEFT_TOP, SegmentMapping.RIGHT_TOP, SegmentMapping.CENTER, SegmentMapping.RIGHT_BOTTOM)),
    NUMBER_5("5", fromSegments(SegmentMapping.TOP, SegmentMapping.LEFT_TOP, SegmentMapping.CENTER, SegmentMapping.RIGHT_BOTTOM, SegmentMapping.BOTTOM)),
    NUMBER_6("6", fromSegments(SegmentMapping.TOP, SegmentMapping.LEFT_TOP, SegmentMapping.CENTER, SegmentMapping.LEFT_BOTTOM, SegmentMapping.RIGHT_BOTTOM, SegmentMapping.BOTTOM)),
    NUMBER_7("7", fromSegments(SegmentMapping.TOP, SegmentMapping.RIGHT_TOP, SegmentMapping.RIGHT_BOTTOM)),
    NUMBER_8("8", fromSegments(SegmentMapping.LEFT_TOP, SegmentMapping.TOP, SegmentMapping.RIGHT_TOP, SegmentMapping.CENTER, SegmentMapping.LEFT_BOTTOM, SegmentMapping.RIGHT_BOTTOM, SegmentMapping.BOTTOM)),
    NUMBER_9("9", fromSegments(SegmentMapping.TOP, SegmentMapping.LEFT_TOP, SegmentMapping.RIGHT_TOP, SegmentMapping.CENTER, SegmentMapping.RIGHT_BOTTOM, SegmentMapping.BOTTOM)),
    CHAR_A("A", fromSegments(SegmentMapping.TOP, SegmentMapping.LEFT_TOP, SegmentMapping.RIGHT_TOP, SegmentMapping.CENTER, SegmentMapping.LEFT_BOTTOM, SegmentMapping.RIGHT_BOTTOM)),
    CHAR_B("B", fromSegments(SegmentMapping.LEFT_TOP, SegmentMapping.CENTER, SegmentMapping.LEFT_BOTTOM, SegmentMapping.RIGHT_BOTTOM, SegmentMapping.BOTTOM)),
    CHAR_C("C", fromSegments(SegmentMapping.TOP, SegmentMapping.LEFT_TOP, SegmentMapping.LEFT_BOTTOM, SegmentMapping.BOTTOM)),
    CHAR_D("D", fromSegments(SegmentMapping.RIGHT_TOP, SegmentMapping.CENTER, SegmentMapping.LEFT_BOTTOM, SegmentMapping.RIGHT_BOTTOM, SegmentMapping.BOTTOM)),
    CHAR_E("E", fromSegments(SegmentMapping.TOP, SegmentMapping.LEFT_TOP, SegmentMapping.CENTER, SegmentMapping.LEFT_BOTTOM, SegmentMapping.BOTTOM)),
    CHAR_F("F", fromSegments(SegmentMapping.TOP, SegmentMapping.LEFT_TOP, SegmentMapping.CENTER, SegmentMapping.LEFT_BOTTOM));

    private final String label;
    private final Byte value;

    SevenSegmentSymbol(String label, Byte value) {
        this.label = label;
        this.value = value;
    }

    /**
     * Helper method for creating a raw digit value (byte) from 0-n segments.
     * This can be used together with the {@link SegmentMapping} enumeration to create and display your own symbol.
     * All segments passed to this method will be flagged as active and enabled when passed to setRawDigit(int, byte).
     *
     * @param segments Segments which should be enabled to together
     * @return Raw digit value as byte
     */
    private static byte fromSegments(SegmentMapping... segments) {
        byte result = 0;
        for (SegmentMapping segment : segments) {
            result |= segment.getValue();
        }
        return result;
    }

    public String getLabel() {
        return label;
    }

    public Byte getValue() {
        return value;
    }

    public String getHexValue() {
        return ("#" + Integer.toHexString(value)).toUpperCase();
    }

    public String getBitsValue() {
        return Integer.toBinaryString(value & 0xFF);
    }
}
