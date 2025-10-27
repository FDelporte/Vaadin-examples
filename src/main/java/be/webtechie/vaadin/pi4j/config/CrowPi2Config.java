package be.webtechie.vaadin.pi4j.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "crowpi.version", havingValue = "2")
public class CrowPi2Config implements CrowPiConfig {

    @Override
    public int getPinLed() {
        return 23;
    }

    @Override
    public int getPinTouch() {
        return 17;
    }

    @Override
    public int getI2cBus() {
        return 1;
    }

    @Override
    public byte getI2cDeviceLcd() {
        return 0x27;
    }

    @Override
    public int getChannelPwmBuzzer() {
        return 2;
    }

    @Override
    public int[] getSevenSegmentDisplayIndexes() {
        return new int[]{0, 2, 4, 6};
    }

    @Override
    public boolean hasRGBMatrix() {
        return false;
    }

    @Override
    public boolean hasDHT11Sensor() {
        return true;
    }

    @Override
    public boolean hasBMX280Sensor() {
        return false;
    }

    @Override
    public Integer getI2cDeviceSensor() {
        return null;
    }
}