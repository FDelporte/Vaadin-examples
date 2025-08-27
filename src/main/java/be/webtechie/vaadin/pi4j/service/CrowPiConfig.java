package be.webtechie.vaadin.pi4j.service;

public enum CrowPiConfig {
    CROWPI_1(22, 17),
    CROWPI_2(21, 17);

    private final int pinLed;
    private final int pinTouch;

    CrowPiConfig(int pinLed, int pinTouch) {
        this.pinLed = pinLed;
        this.pinTouch = pinTouch;
    }

    public int getPinLed() {
        return pinLed;
    }

    public int getPinTouch() {
        return pinTouch;
    }
}
