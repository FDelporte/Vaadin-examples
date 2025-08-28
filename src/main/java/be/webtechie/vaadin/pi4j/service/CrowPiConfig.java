package be.webtechie.vaadin.pi4j.service;

public enum CrowPiConfig {
    CROWPI_1_RPI_4(22, 17, 0, false,
            new int[]{0, 2, 6, 8}),
    CROWPI_2_RPI_5(21, 17, 2, true,
            new int[]{0, 2, 4, 6});

    private final int pinLed;
    private final int pinTouch;
    private final int channelPwmBuzzer;
    private final boolean hasRGBMatrix;
    private final int[] sevenSegmentDisplayIndexes;

    CrowPiConfig(int pinLed, int pinTouch, int channelPwmBuzzer, boolean hasRGBMatrix,
                 int[] sevenSegmentDisplayIndexes) {
        this.pinLed = pinLed;
        this.pinTouch = pinTouch;
        this.channelPwmBuzzer = channelPwmBuzzer;
        this.hasRGBMatrix = hasRGBMatrix;
        this.sevenSegmentDisplayIndexes = sevenSegmentDisplayIndexes;
    }

    public int getPinLed() {
        return pinLed;
    }

    public int getPinTouch() {
        return pinTouch;
    }

    public int getChannelPwmBuzzer() {
        return channelPwmBuzzer;
    }

    public boolean getHasRGBMatrix() {
        return this.hasRGBMatrix;
    }

    public int[] getSevenSegmentDisplayIndexes() {
        return sevenSegmentDisplayIndexes;
    }
}
