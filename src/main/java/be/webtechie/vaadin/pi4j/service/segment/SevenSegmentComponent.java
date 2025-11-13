package be.webtechie.vaadin.pi4j.service.segment;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CImplementation;
import com.pi4j.plugin.ffm.common.HexFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implementation of the CrowPi seven-segment display using I2C with Pi4J
 */
public class SevenSegmentComponent extends HT16K33 {

    /**
     * Mapping of characters to their respective byte representation.
     * Each byte is a bitset where each bit specifies if a specific segment should be enabled (1) or disabled (0).
     */
    protected static final List<SevenSegmentSymbol> CHAR_BITSETS = List.of();
    /**
     * Internal buffer index for the colon of the seven-segment display
     */
    private final int[] sevenSegmentDisplayIndexes;
    private final Logger logger = LoggerFactory.getLogger(SevenSegmentComponent.class);

    /**
     * Creates a new seven-segment display component with a custom bus and device address.
     *
     * @param pi4j   Pi4J context
     * @param bus    Bus address
     * @param device Device address
     */
    public SevenSegmentComponent(Context pi4j, int bus, byte device, int[] sevenSegmentDisplayIndexes) {
        super(pi4j.create(buildI2CConfig(pi4j, bus, device)));
        this.sevenSegmentDisplayIndexes = sevenSegmentDisplayIndexes;
    }

    /**
     * Builds a new I2C instance for the seven-segment display
     *
     * @param pi4j   Pi4J context
     * @param bus    Bus address
     * @param device Device address
     * @return I2C instance
     */
    private static I2CConfig buildI2CConfig(Context pi4j, int bus, byte device) {
        return I2C.newConfigBuilder(pi4j)
                .id("I2C-SEGMENT-" + bus + "-" + HexFormatter.format(device))
                .name("Segment Display")
                .bus(bus)
                .device((int) device)
                .i2cImplementation(I2CImplementation.DIRECT)
                .build();
    }

    /**
     * Sets the symbol on the specified position.
     * This will only affect the internal buffer and does not get displayed until {@link #refresh()} gets called.
     *
     * @param position Desired position of digit from 0-3.
     * @param symbol   {@link SevenSegmentSymbol}
     */
    public void setSymbol(int position, SevenSegmentSymbol symbol) {
        setRawDigit(position, symbol.getValue());
    }

    /**
     * Sets the raw digit at the specified position. This method will take a byte value which gets processed by the underlying chip.
     * The byte represents a bitset where each bit belongs to a specific segment and decides if its enabled (1) or disabled (0).
     * This will only affect the internal buffer and does not get displayed until {@link #refresh()} gets called.
     *
     * @param position Desired position of digit from 0-3.
     * @param value    Raw byte value to be displayed.
     */
    protected void setRawDigit(int position, byte value) {
        var digitIndex = resolveDigitIndex(position);
        logger.info("Setting digit on index {} to {}", digitIndex, value);
        buffer[digitIndex] = value;
    }

    /**
     * Helper method for converting the human-readable position of a digit (e.g. second digit) to the actual buffer index.
     * This will throw an {@link IndexOutOfBoundsException} when the given position is outside of the known indices.
     *
     * @param position Human-readable position of digit starting at zero
     * @return Actual index of digit in the internal buffer
     */
    private int resolveDigitIndex(int position) {
        // Ensure position is within bounds
        final var maxPosition = sevenSegmentDisplayIndexes.length - 1;
        if (position < 0 || position > maxPosition) {
            throw new IndexOutOfBoundsException("Digit position is outside of range 0-" + maxPosition);
        }

        // Lookup actual index based on position
        return sevenSegmentDisplayIndexes[position];
    }
}
