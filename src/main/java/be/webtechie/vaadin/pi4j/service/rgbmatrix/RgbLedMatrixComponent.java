package be.webtechie.vaadin.pi4j.service.rgbmatrix;

import be.webtechie.vaadin.pi4j.service.SleepHelper;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixDirection;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixScrollMode;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixSymbol;
import com.pi4j.context.Context;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of the CrowPi2 RGB LED matrix using PWM/WS2812B with Pi4J V2
 * This targets the 8x8 RGB matrix connected to GPIO26 (PWM0/BCM12) on CrowPi2
 */
public class RgbLedMatrixComponent {

    /**
     * Width and height of the LED matrix
     */
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;
    public static final int TOTAL_LEDS = WIDTH * HEIGHT;

    /**
     * PWM frequency for WS2812B timing (800kHz)
     */
    protected static final int PWM_FREQUENCY = 800000;

    /**
     * Default delay between scroll operations in milliseconds
     */
    protected static final long DEFAULT_SCROLL_DELAY = 50;

    /**
     * Default direction for scroll operations
     */
    protected static final MatrixDirection DEFAULT_SCROLL_MATRIX_DIRECTION = MatrixDirection.LEFT;

    /**
     * RGB color buffer for the matrix (3 bytes per LED: R, G, B)
     */
    private final Color[][] colorBuffer;

    /**
     * Monochrome buffer for compatibility with existing matrix symbols
     */
    private final boolean[][] monoBuffer;

    /**
     * Pi4J PWM instance for controlling WS2812B LEDs
     */
    private final Pwm pwm;

    /**
     * Default brightness (0.0 to 1.0)
     */
    private double brightness = 0.1; // Start dim to avoid power issues

    /**
     * Creates a new RGB LED matrix component with a custom GPIO pin.
     *
     * @param pi4j    Pi4J context
     * @param gpioPin GPIO pin number
     */
    public RgbLedMatrixComponent(Context pi4j, int gpioPin) {
        this.colorBuffer = new Color[HEIGHT][WIDTH];
        this.monoBuffer = new boolean[HEIGHT][WIDTH];

        // Initialize buffers
        clear();

        // Create PWM configuration for WS2812B
        // Note: WS2812B requires precise timing that's difficult with Pi4J PWM
        // This is a conceptual implementation - real usage would need native library
        PwmConfig pwmConfig = Pwm.newConfigBuilder(pi4j)
                .id("WS2812B-PWM")
                .name("RGB Matrix PWM")
                .address(gpioPin)
                .pwmType(PwmType.HARDWARE)
                .frequency(PWM_FREQUENCY)
                .initial(0)
                .shutdown(0)
                .build();

        this.pwm = pi4j.create(pwmConfig);
    }

    /**
     * Gets the current brightness level
     *
     * @return Current brightness (0.0 to 1.0)
     */
    public double getBrightness() {
        return brightness;
    }

    /**
     * Sets the brightness of the matrix (0.0 to 1.0)
     * Note: Be careful with high brightness values as they can draw significant current
     *
     * @param brightness Brightness level (0.0 = off, 1.0 = full brightness)
     */
    public void setBrightness(double brightness) {
        this.brightness = Math.max(0.0, Math.min(1.0, brightness));
    }

    /**
     * Sets a pixel to a specific color
     *
     * @param x     X coordinate (0-7)
     * @param y     Y coordinate (0-7)
     * @param color Color to set
     */
    public void setPixel(int x, int y, Color color) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            colorBuffer[y][x] = color;
            monoBuffer[y][x] = !color.equals(Color.BLACK);
        }
    }

    /**
     * Sets a pixel to on or off (for monochrome compatibility)
     *
     * @param x     X coordinate (0-7)
     * @param y     Y coordinate (0-7)
     * @param state true for on (white), false for off (black)
     */
    public void setPixel(int x, int y, boolean state) {
        setPixel(x, y, state ? Color.WHITE : Color.BLACK);
    }

    /**
     * Gets the color of a specific pixel
     *
     * @param x X coordinate (0-7)
     * @param y Y coordinate (0-7)
     * @return Color of the pixel
     */
    public Color getPixel(int x, int y) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            return colorBuffer[y][x];
        }
        return Color.BLACK;
    }

    /**
     * Clears the entire matrix (all pixels off)
     */
    public void clear() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                colorBuffer[y][x] = Color.BLACK;
                monoBuffer[y][x] = false;
            }
        }
    }

    /**
     * Sets the entire matrix to a single color
     *
     * @param color Color to fill the matrix with
     */
    public void fill(Color color) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                colorBuffer[y][x] = color;
                monoBuffer[y][x] = !color.equals(Color.BLACK);
            }
        }
    }

    /**
     * Refreshes the display by sending the current buffer to the LED matrix
     * This converts the RGB buffer to WS2812B format and sends it via PWM
     */
    public void refresh() {
        // Convert color buffer to WS2812B data format
        byte[] ledData = new byte[TOTAL_LEDS * 3]; // 3 bytes per LED (GRB format for WS2812B)

        int dataIndex = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Color pixel = colorBuffer[y][x];

                // Apply brightness scaling
                int red = (int) (pixel.getRed() * brightness);
                int green = (int) (pixel.getGreen() * brightness);
                int blue = (int) (pixel.getBlue() * brightness);

                // WS2812B expects GRB format
                ledData[dataIndex++] = (byte) green;
                ledData[dataIndex++] = (byte) red;
                ledData[dataIndex++] = (byte) blue;
            }
        }

        // Send data to LEDs via PWM (this is a simplified approach)
        // In a real implementation, you'd need to convert to proper WS2812B timing
        sendWS2812BData(ledData);
    }

    /**
     * Simplified method to send WS2812B data via PWM
     * Note: This is a conceptual implementation. Real WS2812B control requires
     * precise timing that may need native code or specialized libraries
     *
     * @param data RGB data in GRB format
     */
    private void sendWS2812BData(byte[] data) {
        // This is a placeholder implementation
        // Real WS2812B control requires precise timing:
        // - 0 bit: 0.4µs high, 0.85µs low
        // - 1 bit: 0.8µs high, 0.45µs low
        // - Reset: >50µs low

        // For a complete implementation, you would typically use:
        // 1. DMA + PWM for precise timing
        // 2. A native library like rpi_ws281x
        // 3. SPI with carefully crafted bit patterns

        System.out.println("Sending " + data.length + " bytes to WS2812B matrix");
        // Placeholder PWM duty cycle setting
        if (data.length > 0) {
            pwm.on(50, PWM_FREQUENCY); // 50% duty cycle as example
        } else {
            pwm.off();
        }
    }

    /**
     * Scrolls the display towards the given direction and leaves the now empty row/column empty.
     *
     * @param matrixDirection Desired scroll direction
     */
    public void scroll(MatrixDirection matrixDirection) {
        scroll(matrixDirection, MatrixScrollMode.NORMAL, null, 0);
    }

    /**
     * Rotates the display towards the given direction and wraps around the affected row/column.
     *
     * @param matrixDirection Desired scroll direction
     */
    public void rotate(MatrixDirection matrixDirection) {
        scroll(matrixDirection, MatrixScrollMode.ROTATE, null, 0);
    }

    /**
     * Internal scroll method that handles different scroll modes
     */
    protected void scroll(MatrixDirection matrixDirection, MatrixScrollMode matrixScrollMode,
                          Color[][] newColorBuffer, int newOffset) {
        switch (matrixDirection) {
            case UP:
                scrollUp(matrixScrollMode, newColorBuffer, newOffset);
                break;
            case DOWN:
                scrollDown(matrixScrollMode, newColorBuffer, newOffset);
                break;
            case LEFT:
                scrollLeft(matrixScrollMode, newColorBuffer, newOffset);
                break;
            case RIGHT:
                scrollRight(matrixScrollMode, newColorBuffer, newOffset);
                break;
        }
        refresh();
    }

    private void scrollUp(MatrixScrollMode matrixScrollMode, Color[][] newColorBuffer, int newOffset) {
        // Save first row
        Color[] firstRow = colorBuffer[0].clone();

        // Shift all rows up
        for (int y = 0; y < HEIGHT - 1; y++) {
            System.arraycopy(colorBuffer[y + 1], 0, colorBuffer[y], 0, WIDTH);
        }

        // Handle last row based on scroll mode
        Color[] lastRow = new Color[WIDTH];
        if (matrixScrollMode == MatrixScrollMode.ROTATE) {
            lastRow = firstRow;
        } else if (matrixScrollMode == MatrixScrollMode.REPLACE && newColorBuffer != null) {
            lastRow = newColorBuffer[newOffset].clone();
        } else {
            for (int x = 0; x < WIDTH; x++) {
                lastRow[x] = Color.BLACK;
            }
        }
        colorBuffer[HEIGHT - 1] = lastRow;

        updateMonoBuffer();
    }

    private void scrollDown(MatrixScrollMode matrixScrollMode, Color[][] newColorBuffer, int newOffset) {
        // Save last row
        Color[] lastRow = colorBuffer[HEIGHT - 1].clone();

        // Shift all rows down
        for (int y = HEIGHT - 1; y > 0; y--) {
            System.arraycopy(colorBuffer[y - 1], 0, colorBuffer[y], 0, WIDTH);
        }

        // Handle first row based on scroll mode
        Color[] firstRow = new Color[WIDTH];
        if (matrixScrollMode == MatrixScrollMode.ROTATE) {
            firstRow = lastRow;
        } else if (matrixScrollMode == MatrixScrollMode.REPLACE && newColorBuffer != null) {
            firstRow = newColorBuffer[HEIGHT - 1 - newOffset].clone();
        } else {
            for (int x = 0; x < WIDTH; x++) {
                firstRow[x] = Color.BLACK;
            }
        }
        colorBuffer[0] = firstRow;

        updateMonoBuffer();
    }

    private void scrollLeft(MatrixScrollMode matrixScrollMode, Color[][] newColorBuffer, int newOffset) {
        for (int y = 0; y < HEIGHT; y++) {
            Color firstPixel = colorBuffer[y][0];

            // Shift row left
            for (int x = 0; x < WIDTH - 1; x++) {
                colorBuffer[y][x] = colorBuffer[y][x + 1];
            }

            // Handle last column
            if (matrixScrollMode == MatrixScrollMode.ROTATE) {
                colorBuffer[y][WIDTH - 1] = firstPixel;
            } else if (matrixScrollMode == MatrixScrollMode.REPLACE && newColorBuffer != null) {
                colorBuffer[y][WIDTH - 1] = newColorBuffer[y][WIDTH - 1 - newOffset];
            } else {
                colorBuffer[y][WIDTH - 1] = Color.BLACK;
            }
        }

        updateMonoBuffer();
    }

    private void scrollRight(MatrixScrollMode matrixScrollMode, Color[][] newColorBuffer, int newOffset) {
        for (int y = 0; y < HEIGHT; y++) {
            Color lastPixel = colorBuffer[y][WIDTH - 1];

            // Shift row right
            for (int x = WIDTH - 1; x > 0; x--) {
                colorBuffer[y][x] = colorBuffer[y][x - 1];
            }

            // Handle first column
            if (matrixScrollMode == MatrixScrollMode.ROTATE) {
                colorBuffer[y][0] = lastPixel;
            } else if (matrixScrollMode == MatrixScrollMode.REPLACE && newColorBuffer != null) {
                colorBuffer[y][0] = newColorBuffer[y][newOffset];
            } else {
                colorBuffer[y][0] = Color.BLACK;
            }
        }

        updateMonoBuffer();
    }

    /**
     * Updates the monochrome buffer based on the color buffer
     */
    private void updateMonoBuffer() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                monoBuffer[y][x] = !colorBuffer[y][x].equals(Color.BLACK);
            }
        }
    }

    /**
     * Prints a string with default settings (white color, left scroll)
     *
     * @param string String to display
     */
    public void print(String string) {
        print(string, Color.WHITE);
    }

    /**
     * Prints a string in the specified color
     *
     * @param string String to display
     * @param color  Color to use
     */
    public void print(String string, Color color) {
        print(string, color, DEFAULT_SCROLL_MATRIX_DIRECTION, DEFAULT_SCROLL_DELAY);
    }

    /**
     * Prints a string with full customization
     *
     * @param string                String to display
     * @param color                 Color to use
     * @param scrollMatrixDirection Direction to scroll
     * @param scrollDelay           Delay between scroll steps
     */
    public void print(String string, Color color, MatrixDirection scrollMatrixDirection, long scrollDelay) {
        final var symbols = convertToMatrixSymbols(string);

        // Clear display
        clear();
        refresh();

        // Display each symbol
        for (MatrixSymbol symbol : symbols) {
            transition(symbol, color, scrollMatrixDirection, scrollDelay);
        }

        // Clear at end
        transition(MatrixSymbol.SPACE, color, scrollMatrixDirection, scrollDelay);
    }

    /**
     * Displays a MatrixSymbol in the specified color
     *
     * @param symbol Symbol to display
     * @param color  Color to use
     */
    public void print(MatrixSymbol symbol, Color color) {
        clear();
        byte[] rows = symbol.getRows();
        for (int y = 0; y < HEIGHT && y < rows.length; y++) {
            byte row = rows[y];
            for (int x = 0; x < WIDTH; x++) {
                boolean pixelOn = ((row >> (WIDTH - 1 - x)) & 1) == 1;
                setPixel(x, y, pixelOn ? color : Color.BLACK);
            }
        }
        refresh();
    }

    /**
     * Transitions to a new symbol with color support
     */
    public void transition(MatrixSymbol symbol, Color color, MatrixDirection direction, long delay) {
        // Convert symbol to color buffer
        Color[][] symbolBuffer = new Color[HEIGHT][WIDTH];
        byte[] rows = symbol.getRows();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (y < rows.length) {
                    boolean pixelOn = ((rows[y] >> (WIDTH - 1 - x)) & 1) == 1;
                    symbolBuffer[y][x] = pixelOn ? color : Color.BLACK;
                } else {
                    symbolBuffer[y][x] = Color.BLACK;
                }
            }
        }

        // Scroll in the symbol
        for (int i = 0; i < WIDTH; i++) {
            scroll(direction, MatrixScrollMode.REPLACE, symbolBuffer, i);
            SleepHelper.sleep(delay);
        }
    }

    /**
     * Converts string to MatrixSymbol list (reusing existing logic)
     */
    protected List<MatrixSymbol> convertToMatrixSymbols(String string) {
        final List<MatrixSymbol> symbols = new ArrayList<>();
        final StringBuilder buffer = new StringBuilder();
        boolean referenceMode = false;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c == '{') {
                referenceMode = true;
            } else if (referenceMode && c == '}') {
                try {
                    final var symbolName = buffer.toString().toUpperCase();
                    final var symbol = MatrixSymbol.valueOf(symbolName);
                    symbols.add(symbol);
                } catch (IllegalArgumentException e) {
                    symbols.add(MatrixSymbol.BRACE_LEFT);
                    for (int j = 0; j < buffer.length(); j++) {
                        symbols.add(lookupMatrixSymbol(buffer.charAt(j)));
                    }
                    symbols.add(MatrixSymbol.BRACE_RIGHT);
                } finally {
                    buffer.delete(0, buffer.length());
                    referenceMode = false;
                }
            } else if (referenceMode) {
                buffer.append(c);
            } else {
                symbols.add(lookupMatrixSymbol(c));
            }
        }

        if (referenceMode) {
            symbols.add(MatrixSymbol.BRACE_LEFT);
            for (int i = 0; i < buffer.length(); i++) {
                symbols.add(lookupMatrixSymbol(buffer.charAt(i)));
            }
        }

        return symbols;
    }

    protected MatrixSymbol lookupMatrixSymbol(char c) {
        final var symbol = MatrixSymbol.getByChar(c);
        if (symbol == null) {
            throw new IllegalArgumentException("Character is not supported by LED matrix");
        }
        return symbol;
    }

    /**
     * Drawing support for RGB images
     */
    public void draw(Consumer<Graphics2D> drawer) {
        final var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        final var graphics = image.createGraphics();
        drawer.accept(graphics);
        draw(image);
    }

    public void draw(BufferedImage image) {
        if (image.getWidth() != WIDTH || image.getHeight() != HEIGHT) {
            throw new IllegalArgumentException("Image must be exactly " + WIDTH + "x" + HEIGHT + " pixels");
        }

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                setPixel(x, y, pixelColor);
            }
        }
        refresh();
    }
}
