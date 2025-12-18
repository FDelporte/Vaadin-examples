package be.webtechie.vaadin.pi4j.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "crowpi.version", havingValue = "1")
public class CrowPi1Config implements CrowPiConfig {

    private static final Logger logger = LoggerFactory.getLogger(CrowPi1Config.class);

    public CrowPi1Config() {
        logger.info("CrowPi 1 configured");
    }

    @Override
    public int getPinLed() {
        return 22;
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
    public byte getI2cDeviceHumidityTemperatureSensor() {
        return 0x00;
    }

    @Override
    public byte getI2cDeviceSevenSegmentDisplay() {
        return 0x70;
    }

    @Override
    public int[] getSevenSegmentDisplayIndexes() {
        return new int[]{0, 2, 6, 8};
    }

    @Override
    public boolean hasRGBMatrix() {
        return false;
    }

    @Override
    public int getPwmChip() {
        return 0;
    }

    @Override
    public int getPwmChannelBuzzer() {
        return 0;
    }

    @Override
    public int getPwmChannelRgbMatrix() {
        return 0;
    }
}