package be.webtechie.vaadin.pi4j.config;

public interface CrowPiConfig {

    int getPinLed();

    int getPinTouch();

    int getI2cBus();

    int getI2cDeviceLcd();

    int getChannelPwmBuzzer();

    int[] getSevenSegmentDisplayIndexes();

    boolean hasRGBMatrix();

    boolean hasDHT11Sensor();

    boolean hasBMX280Sensor();

    Integer getI2cDeviceSensor();
}