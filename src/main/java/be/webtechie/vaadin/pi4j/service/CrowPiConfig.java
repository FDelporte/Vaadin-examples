package be.webtechie.vaadin.pi4j.service;

public enum CrowPiConfig {
    CROWPI_1(22, 17, 18),
    CROWPI_2(21, 17, 2);

    private final int pinLed;
    private final int pinTouch;
    private final int channelPwmBuzzer;

    CrowPiConfig(int pinLed, int pinTouch, int channelPwmBuzzer) {
        this.pinLed = pinLed;
        this.pinTouch = pinTouch;
        this.channelPwmBuzzer = channelPwmBuzzer;
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
}
