package be.webtechie.vaadin.pi4j.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "board.type", havingValue = "crowpi-3")
public class CrowPi3Config implements BoardConfig {

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

    // CrowPi 3 has all standard CrowPi features plus RGB matrix
    // Use default implementations for: hasLed, hasTouch, hasLcd, hasSevenSegment, hasBuzzer, hasDht11
    // Override hasKey (no key on CrowPi)
    @Override
    public boolean hasKey() {
        return false;
    }

    @Override
    public boolean hasOled() {
        return false;
    }

    @Override
    public boolean hasRedMatrix() {
        return false; // CrowPi 3 has RGB matrix instead of red matrix
    }
}
