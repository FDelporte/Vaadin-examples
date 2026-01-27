package be.webtechie.vaadin.pi4j.config;

/**
 * Configuration interface for different hardware boards (CrowPi, Pioneer600, etc.).
 * Implementations provide hardware-specific pin configurations and feature availability flags.
 */
public interface BoardConfig {

    // GPIO pins
    int getPinLed();

    /**
     * Returns the GPIO pin for the key/button input.
     * Default implementation returns -1 (not available).
     */
    default int getPinKey() {
        return -1;
    }

    int getPinTouch();

    // I2C configuration
    int getI2cBus();

    byte getI2cDeviceLcd();

    byte getI2cDeviceHumidityTemperatureSensor();

    byte getI2cDeviceSevenSegmentDisplay();

    int[] getSevenSegmentDisplayIndexes();

    // PWM configuration
    int getPwmChip();

    int getPwmChannelBuzzer();

    int getPwmChannelRgbMatrix();

    boolean hasRGBMatrix();

    // OLED configuration (SSD1306 via SPI)
    /**
     * Returns the GPIO pin for OLED Data/Command signal.
     * Default implementation returns -1 (not available).
     */
    default int getOledDcPin() {
        return -1;
    }

    /**
     * Returns the GPIO pin for OLED Reset signal.
     * Default implementation returns -1 (not available).
     */
    default int getOledRstPin() {
        return -1;
    }

    /**
     * Returns the SPI channel for OLED display.
     * Default implementation returns 0.
     */
    default int getOledSpiChannel() {
        return 0;
    }

    // Feature availability flags
    /**
     * Returns true if the board has an LED.
     * Default implementation returns true.
     */
    default boolean hasLed() {
        return true;
    }

    /**
     * Returns true if the board has a key/button input.
     * Default implementation returns false.
     */
    default boolean hasKey() {
        return false;
    }

    /**
     * Returns true if the board has a touch sensor.
     * Default implementation returns true.
     */
    default boolean hasTouch() {
        return true;
    }

    /**
     * Returns true if the board has an LCD display.
     * Default implementation returns true.
     */
    default boolean hasLcd() {
        return true;
    }

    /**
     * Returns true if the board has a seven segment display.
     * Default implementation returns true.
     */
    default boolean hasSevenSegment() {
        return true;
    }

    /**
     * Returns true if the board has a buzzer.
     * Default implementation returns true.
     */
    default boolean hasBuzzer() {
        return true;
    }

    /**
     * Returns true if the board has a red LED matrix.
     * Default implementation returns true.
     */
    default boolean hasRedMatrix() {
        return true;
    }

    /**
     * Returns true if the board has an OLED display.
     * Default implementation returns false.
     */
    default boolean hasOled() {
        return false;
    }

    /**
     * Returns true if the board has a DHT11 temperature/humidity sensor.
     * Default implementation returns true.
     */
    default boolean hasDht11() {
        return true;
    }
}
