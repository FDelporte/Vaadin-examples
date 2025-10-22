package be.webtechie.vaadin.pi4j.service;

/**
 * This demo application has been used with different demo hardware setups.
 * This configuration enum can be used to configure some of the electronic components.
 * Based on this configuration, some of the UI views are not shown in the demo application.
 */
public enum DemoSetupConfig {
    CROWPI_1_RPI_4(1, 22, 17, 0, false,
            new int[]{0, 2, 6, 8}),
    CROWPI_2_RPI_5(2, 23, 17, 2, true,
            new int[]{0, 2, 4, 6}),
    // https://www.elecrow.com/wiki/crowpi_3.html#raspberry33v
    CROWPI_3_RPI_5(3, 21, 17, 2, true,
            new int[]{0, 2, 4, 6});

    /**
     * Use 0 or lower if not a CrowPi
     */
    private final int crowPiVersion;
    private final int pinLed;
    private final int pinTouch;
    private final int channelPwmBuzzer;
    private final boolean hasRGBMatrix;
    private final int[] sevenSegmentDisplayIndexes;

    DemoSetupConfig(int crowPiVersion, int pinLed, int pinTouch, int channelPwmBuzzer, boolean hasRGBMatrix,
                    int[] sevenSegmentDisplayIndexes) {
        this.crowPiVersion = crowPiVersion;
        this.pinLed = pinLed;
        this.pinTouch = pinTouch;
        this.channelPwmBuzzer = channelPwmBuzzer;
        this.hasRGBMatrix = hasRGBMatrix;
        this.sevenSegmentDisplayIndexes = sevenSegmentDisplayIndexes;
    }

    public boolean isCrowPiVersion() {
        return crowPiVersion > 0;
    }

    public int getCrowPiVersion() {
        return crowPiVersion;
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
