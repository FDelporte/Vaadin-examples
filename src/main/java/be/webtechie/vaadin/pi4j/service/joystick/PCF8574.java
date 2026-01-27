package be.webtechie.vaadin.pi4j.service.joystick;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PCF8574 8-bit I/O expander driver.
 * On Pioneer600, the PCF8574 is connected to:
 * - P0-P3: Joystick directions (active low)
 * - P4: LED control
 * - P7: Beeper control
 */
public class PCF8574 {

    private static final Logger logger = LoggerFactory.getLogger(PCF8574.class);

    public static final int DEFAULT_I2C_ADDRESS = 0x20;

    // Joystick direction bit masks (active low)
    private static final int JOYSTICK_LEFT = 0xFE;   // P0 low
    private static final int JOYSTICK_UP = 0xFD;     // P1 low
    private static final int JOYSTICK_DOWN = 0xFB;   // P2 low
    private static final int JOYSTICK_RIGHT = 0xF7;  // P3 low

    // Control bit masks
    private static final int LED_BIT = 0x10;         // P4
    private static final int BEEPER_BIT = 0x80;      // P7

    private final I2C i2c;

    /**
     * Creates a new PCF8574 instance.
     *
     * @param pi4j    Pi4J context
     * @param i2cBus  I2C bus number
     * @param address I2C device address
     */
    public PCF8574(Context pi4j, int i2cBus, int address) {
        var i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("PCF8574-I2C-" + i2cBus + "-0x" + Integer.toHexString(address))
                .name("PCF8574 I/O Expander")
                .bus(i2cBus)
                .device(address)
                .build();
        logger.info("Creating I2C device on bus {} at address 0x{}", i2cBus, Integer.toHexString(address));
        this.i2c = pi4j.create(i2cConfig);
        logger.info("PCF8574 initialized on I2C bus {} at address 0x{}", i2cBus, Integer.toHexString(address));
    }

    /**
     * Reads the current joystick direction.
     *
     * @return the current joystick direction, or NONE if no direction is pressed
     */
    public JoystickDirection readJoystick() {
        // Set P0-P3 as inputs by writing high bits
        int current = readByte();
        int writeVal = 0x0F | current;
        writeByte(writeVal);

        // Read and mask the value
        int rawValue = readByte();
        int value = rawValue | 0xF0;

        if (value == 0xFF) {
            return JoystickDirection.NONE;
        }

        // Log when joystick activity is detected
        logger.info("readJoystick: current=0x{}, wrote=0x{}, rawRead=0x{}, masked=0x{}",
                Integer.toHexString(current), Integer.toHexString(writeVal),
                Integer.toHexString(rawValue), Integer.toHexString(value));

        return switch (value) {
            case JOYSTICK_LEFT -> JoystickDirection.LEFT;
            case JOYSTICK_UP -> JoystickDirection.UP;
            case JOYSTICK_DOWN -> JoystickDirection.DOWN;
            case JOYSTICK_RIGHT -> JoystickDirection.RIGHT;
            default -> {
                logger.info("Unknown joystick value: 0x{}", Integer.toHexString(value));
                yield JoystickDirection.NONE;
            }
        };
    }

    /**
     * Turns the LED on or off.
     *
     * @param on true to turn LED on, false to turn off
     */
    public void setLed(boolean on) {
        int current = readByte();
        if (on) {
            writeByte(current & ~LED_BIT);  // Clear bit to turn on
        } else {
            writeByte(current | LED_BIT);   // Set bit to turn off
        }
    }

    /**
     * Turns the beeper on or off.
     *
     * @param on true to turn beeper on, false to turn off
     */
    public void setBeeper(boolean on) {
        int current = readByte();
        int newValue = on ? (current & ~BEEPER_BIT) : (current | BEEPER_BIT);
        logger.info("setBeeper({}): current=0x{}, writing=0x{}", on,
                Integer.toHexString(current), Integer.toHexString(newValue));
        writeByte(newValue);
    }

    private int readByte() {
        try {
            // PCF8574 doesn't have registers - read directly using a 1-byte buffer
            byte[] buffer = new byte[1];
            int bytesRead = i2c.read(buffer, 0, 1);
            if (bytesRead != 1) {
                logger.warn("I2C read returned {} bytes instead of 1", bytesRead);
            }
            return buffer[0] & 0xFF;
        } catch (Exception e) {
            logger.error("I2C read failed: {}", e.getMessage());
            throw e;
        }
    }

    private void writeByte(int value) {
        try {
            // PCF8574 doesn't have registers - write directly
            i2c.write((byte) value);
        } catch (Exception e) {
            logger.error("I2C write failed for value 0x{}: {}", Integer.toHexString(value), e.getMessage());
            throw e;
        }
    }
}
