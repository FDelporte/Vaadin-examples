package be.webtechie.vaadin.pi4j.config;

public interface CrowPiConfig {

    int getPinLed();

    int getPinTouch();

    int getI2cBus();

    byte getI2cDeviceLcd();

    int getChannelPwmBuzzer();

    int[] getSevenSegmentDisplayIndexes();

    boolean hasRGBMatrix();

    Integer getI2cDeviceSensor();
}