package be.webtechie.vaadin.pi4j.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(CrowPi3Config.class)
public class CrowPi3Config implements CrowPiConfig {

    private static final Logger logger = LoggerFactory.getLogger(CrowPi3Config.class);

    public CrowPi3Config() {
        logger.info("CrowPi 3 configured");
    }

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
        return 0x21;
    }

    @Override
    public byte getI2cDeviceHumidityTemperatureSensor() {
        return 0x38;
    }

    @Override
    public byte getI2cDeviceSevenSegmentDisplay() {
        return 0x70;
    }

    @Override
    public int[] getSevenSegmentDisplayIndexes() {
        return new int[]{0, 2, 4, 6};
    }

    @Override
    public boolean hasRGBMatrix() {
        return true;
    }

    @Override
    public int getPwmChip() {
        return 0;
    }

    @Override
    public int getPwmChannelBuzzer() {
        return 2;
    }

    @Override
    public int getPwmChannelRgbMatrix() {
        return 0;
    }
}