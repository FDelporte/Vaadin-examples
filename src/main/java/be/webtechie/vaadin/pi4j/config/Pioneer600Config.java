package be.webtechie.vaadin.pi4j.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Pioneer600 expansion board.
 *
 * Pioneer600 features supported:
 * - LED (GPIO 26)
 * - KEY/Button (GPIO 20)
 * - OLED display (SSD1306 via SPI, DC=16, RST=19)
 * - BMP280 barometric pressure sensor (I2C address 0x76)
 * - Joystick via PCF8574 I/O expander (I2C address 0x20)
 *
 * Features NOT available on Pioneer600:
 * - Touch sensor, LCD display, seven segment display
 * - Buzzer, LED matrix, DHT11 sensor
 */
@Configuration
@ConditionalOnProperty(name = "board.type", havingValue = "pioneer600")
public class Pioneer600Config implements BoardConfig {

    private static final Logger logger = LoggerFactory.getLogger(Pioneer600Config.class);

    public Pioneer600Config() {
        logger.info("Pioneer600 board configured");
    }

    // GPIO pins
    @Override
    public int getPinLed() {
        return 26;
    }

    @Override
    public int getPinKey() {
        return 20;
    }

    @Override
    public int getPinTouch() {
        return -1; // Not available on Pioneer600
    }

    // I2C configuration - not used on Pioneer600 for CrowPi components
    @Override
    public int getI2cBus() {
        return 1;
    }

    @Override
    public byte getI2cDeviceLcd() {
        return 0x00; // Not available
    }

    @Override
    public byte getI2cDeviceHumidityTemperatureSensor() {
        return 0x00; // Not available
    }

    @Override
    public byte getI2cDeviceSevenSegmentDisplay() {
        return 0x00; // Not available
    }

    @Override
    public int[] getSevenSegmentDisplayIndexes() {
        return new int[0]; // Not available
    }

    // PWM configuration - not used on Pioneer600 for CrowPi components
    @Override
    public int getPwmChip() {
        return 0;
    }

    @Override
    public int getPwmChannelBuzzer() {
        return 0; // Not available
    }

    @Override
    public int getPwmChannelRgbMatrix() {
        return 0; // Not available
    }

    @Override
    public boolean hasRGBMatrix() {
        return false;
    }

    // OLED configuration (SSD1306 via SPI)
    @Override
    public int getOledDcPin() {
        return 16;
    }

    @Override
    public int getOledRstPin() {
        return 19;
    }

    @Override
    public int getOledSpiChannel() {
        return 0;
    }

    // Feature availability flags
    @Override
    public boolean hasLed() {
        return true;
    }

    @Override
    public boolean hasKey() {
        return true;
    }

    @Override
    public boolean hasTouch() {
        return false;
    }

    @Override
    public boolean hasLcd() {
        return false;
    }

    @Override
    public boolean hasSevenSegment() {
        return false;
    }

    @Override
    public boolean hasBuzzer() {
        return false;
    }

    @Override
    public boolean hasRedMatrix() {
        return false;
    }

    @Override
    public boolean hasOled() {
        return true;
    }

    @Override
    public boolean hasDht11() {
        return false;
    }

    // BMP280 barometric pressure sensor (I2C)
    @Override
    public byte getI2cDeviceBmp280() {
        return 0x76;
    }

    @Override
    public boolean hasBmp280() {
        return true;
    }

    // PCF8574 I/O expander for joystick (I2C)
    @Override
    public byte getI2cDevicePcf8574() {
        return 0x20;
    }

    @Override
    public boolean hasJoystick() {
        return true;
    }

    @Override
    public boolean hasPcf8574Buzzer() {
        return true;
    }
}
