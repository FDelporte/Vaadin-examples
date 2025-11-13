package be.webtechie.vaadin.pi4j.config;

public interface CrowPiConfig {

    int getPinLed();

    int getPinTouch();

    int getI2cBus();

    byte getI2cDeviceLcd();

    byte getI2cDeviceHumidityTemperatureSensor();

    byte getI2cDeviceSevenSegmentDisplay();

    int[] getSevenSegmentDisplayIndexes();

    boolean hasRGBMatrix();

    int getPwmBus();

    int getPwmChannelBuzzer();

    int getPwmChannelRgbMatrix();
}