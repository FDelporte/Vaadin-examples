package be.webtechie.vaadin.pi4j.service.oled;

import be.webtechie.vaadin.pi4j.service.SleepHelper;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiConfig;

import java.awt.image.BufferedImage;

/**
 * Java helper class for SSD1306 OLED display using Pi4J library.
 * This is a port of the Python SSD1306 driver to work with Pi4J and Java.
 *
 * The SSD1306 is a 128x64 pixel OLED display commonly used with Raspberry Pi projects.
 * It communicates via SPI and requires DC (Data/Command) and RST (Reset) GPIO pins.
 */
public class SSD1306 {

    // SSD1306 Command constants
    private static final int SSD1306_SETCONTRAST = 0x81;
    private static final int SSD1306_DISPLAYALLON_RESUME = 0xA4;
    private static final int SSD1306_DISPLAYALLON = 0xA5;
    private static final int SSD1306_NORMALDISPLAY = 0xA6;
    private static final int SSD1306_INVERTDISPLAY = 0xA7;
    private static final int SSD1306_DISPLAYOFF = 0xAE;
    private static final int SSD1306_DISPLAYON = 0xAF;
    private static final int SSD1306_SETDISPLAYOFFSET = 0xD3;
    private static final int SSD1306_SETCOMPINS = 0xDA;
    private static final int SSD1306_SETVCOMDETECT = 0xDB;
    private static final int SSD1306_SETDISPLAYCLOCKDIV = 0xD5;
    private static final int SSD1306_SETPRECHARGE = 0xD9;
    private static final int SSD1306_SETMULTIPLEX = 0xA8;
    private static final int SSD1306_SETLOWCOLUMN = 0x00;
    private static final int SSD1306_SETHIGHCOLUMN = 0x10;
    private static final int SSD1306_SETSTARTLINE = 0x40;
    private static final int SSD1306_MEMORYMODE = 0x20;
    private static final int SSD1306_COLUMNADDR = 0x21;
    private static final int SSD1306_PAGEADDR = 0x22;
    private static final int SSD1306_COMSCANINC = 0xC0;
    private static final int SSD1306_COMSCANDEC = 0xC8;
    private static final int SSD1306_SEGREMAP = 0xA0;
    private static final int SSD1306_CHARGEPUMP = 0x8D;
    private static final int SSD1306_EXTERNALVCC = 0x1;
    private static final int SSD1306_SWITCHCAPVCC = 0x2;

    // Default SPI settings
    private static final int DEFAULT_SPI_CHANNEL = 0;
    private static final int DEFAULT_SPI_BAUD_RATE = 8000000; // 8MHz

    // Display dimensions
    public static final int WIDTH = 128;
    public static final int HEIGHT = 64;
    private static final int PAGES = 8; // HEIGHT / 8

    // Pi4J components
    private final Context pi4j;
    private final Spi spi;
    private final DigitalOutput dcPin;
    private final DigitalOutput resetPin;

    // Display state
    private final byte[] buffer;
    private int vccState;

    /**
     * Creates a new SSD1306 display instance with default SPI settings.
     *
     * @param pi4j Pi4J context
     * @param dcPin Data/Command GPIO pin number
     * @param resetPin Reset GPIO pin number
     */
    public SSD1306(Context pi4j, int dcPin, int resetPin) {
        this(pi4j, DEFAULT_SPI_CHANNEL, DEFAULT_SPI_BAUD_RATE, dcPin, resetPin);
    }

    /**
     * Creates a new SSD1306 display instance with custom SPI settings.
     *
     * @param pi4j Pi4J context
     * @param spiChannel SPI channel (0 or 1)
     * @param spiBaudRate SPI baud rate
     * @param dcPin Data/Command GPIO pin number
     * @param resetPin Reset GPIO pin number
     */
    public SSD1306(Context pi4j, int spiChannel, int spiBaudRate, int dcPin, int resetPin) {
        this.pi4j = pi4j;
        this.buffer = new byte[WIDTH * PAGES];

        // Initialize SPI
        SpiConfig spiConfig = Spi.newConfigBuilder(pi4j)
                .id("SSD1306-SPI")
                .name("SSD1306 OLED Display")
                .address(spiChannel)
                .baud(spiBaudRate)
                .build();
        this.spi = pi4j.create(spiConfig);

        // Initialize DC pin
        var dcConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("SSD1306-DC")
                .name("SSD1306 DC Pin")
                .address(dcPin)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .build();
        this.dcPin = pi4j.create(dcConfig);

        // Initialize Reset pin
        var resetConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("SSD1306-RST")
                .name("SSD1306 Reset Pin")
                .address(resetPin)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.HIGH)
                .build();
        this.resetPin = pi4j.create(resetConfig);
    }

    /**
     * Sends a command byte to the display.
     *
     * @param cmd Command byte to send
     */
    private void command(int cmd) {
        dcPin.setState(DigitalState.LOW.value().intValue());
        spi.write((byte) cmd);
    }

    /**
     * Sends a data byte to the display.
     *
     * @param val Data byte to send
     */
    private void data(int val) {
        dcPin.setState(DigitalState.HIGH.value().intValue());
        spi.write((byte) val);
    }

    /**
     * Initializes the display with default settings.
     */
    public void begin() {
        begin(SSD1306_SWITCHCAPVCC);
    }

    /**
     * Initializes the display.
     *
     * @param vccState VCC state (SSD1306_SWITCHCAPVCC or SSD1306_EXTERNALVCC)
     */
    public void begin(int vccState) {
        this.vccState = vccState;
        reset();

        command(SSD1306_DISPLAYOFF);                    // 0xAE
        command(SSD1306_SETDISPLAYCLOCKDIV);            // 0xD5
        command(0x80);                                  // the suggested ratio 0x80

        command(SSD1306_SETMULTIPLEX);                  // 0xA8
        command(0x3F);
        command(SSD1306_SETDISPLAYOFFSET);              // 0xD3
        command(0x0);                                   // no offset
        command(SSD1306_SETSTARTLINE | 0x0);            // line #0
        command(SSD1306_CHARGEPUMP);                    // 0x8D
        if (this.vccState == SSD1306_EXTERNALVCC) {
            command(0x10);
        } else {
            command(0x14);
        }
        command(SSD1306_MEMORYMODE);                    // 0x20
        command(0x00);                                  // 0x0 act like ks0108
        command(SSD1306_SEGREMAP | 0x1);
        command(SSD1306_COMSCANDEC);
        command(SSD1306_SETCOMPINS);                    // 0xDA
        command(0x12);
        command(SSD1306_SETCONTRAST);                   // 0x81
        if (this.vccState == SSD1306_EXTERNALVCC) {
            command(0x9F);
        } else {
            command(0xCF);
        }
        command(SSD1306_SETPRECHARGE);                  // 0xd9
        if (this.vccState == SSD1306_EXTERNALVCC) {
            command(0x22);
        } else {
            command(0xF1);
        }
        command(SSD1306_SETVCOMDETECT);                 // 0xDB
        command(0x40);
        command(SSD1306_DISPLAYALLON_RESUME);           // 0xA4
        command(SSD1306_NORMALDISPLAY);                 // 0xA6
        command(SSD1306_DISPLAYON);                     // turn on OLED panel
    }

    /**
     * Resets the display by toggling the reset pin.
     */
    public void reset() {
        resetPin.setState(DigitalState.HIGH.value().intValue());
        SleepHelper.sleep(1);
        resetPin.setState(DigitalState.LOW.value().intValue());
        SleepHelper.sleep(10);
        resetPin.setState(DigitalState.HIGH.value().intValue());
    }

    /**
     * Writes the display buffer to the physical display.
     */
    public void display() {
        command(SSD1306_COLUMNADDR);
        command(0);                         // Column start address
        command(WIDTH - 1);                 // Column end address
        command(SSD1306_PAGEADDR);
        command(0);                         // Page start address
        command(PAGES - 1);                 // Page end address

        // Write buffer data
        dcPin.setState(DigitalState.HIGH.value().intValue());
        spi.write(buffer);
    }

    /**
     * Sets the display buffer from a BufferedImage.
     * The image must be in 1-bit mode and have the same dimensions as the display.
     *
     * @param image BufferedImage to display (must be 128x64 and 1-bit)
     */
    public void image(BufferedImage image) {
        if (image.getWidth() != WIDTH || image.getHeight() != HEIGHT) {
            throw new IllegalArgumentException("Image must be " + WIDTH + "x" + HEIGHT + " pixels");
        }

        // Clear buffer first
        clear();

        // Convert image to display buffer format
        int index = 0;
        for (int page = 0; page < PAGES; page++) {
            for (int x = 0; x < WIDTH; x++) {
                int bits = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int y = page * 8 + (7 - bit);
                    if (y < HEIGHT) {
                        // Check if pixel is "on" (non-black)
                        int rgb = image.getRGB(x, y);
                        boolean pixelOn = (rgb & 0xFFFFFF) != 0;
                        bits = (bits << 1) | (pixelOn ? 1 : 0);
                    }
                }
                buffer[index++] = (byte) bits;
            }
        }
    }

    /**
     * Clears the contents of the image buffer.
     */
    public void clear() {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = 0;
        }
    }

    /**
     * Sets the contrast of the display.
     *
     * @param contrast Contrast value between 0 and 255
     */
    public void setContrast(int contrast) {
        if (contrast < 0 || contrast > 255) {
            throw new IllegalArgumentException("Contrast must be a value from 0 to 255");
        }
        command(SSD1306_SETCONTRAST);
        command(contrast);
    }

    /**
     * Adjusts contrast to dim the display.
     *
     * @param dim true to dim the display, false for normal brightness
     */
    public void dim(boolean dim) {
        int contrast = 0;
        if (!dim) {
            if (this.vccState == SSD1306_EXTERNALVCC) {
                contrast = 0x9F;
            } else {
                contrast = 0xCF;
            }
        }
        setContrast(contrast);
    }

    /**
     * Sets a single pixel in the buffer.
     *
     * @param x X coordinate (0-127)
     * @param y Y coordinate (0-63)
     * @param on true to turn pixel on, false to turn off
     */
    public void setPixel(int x, int y, boolean on) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return; // Outside bounds
        }

        int page = y / 8;
        int bit = y % 8;
        int index = page * WIDTH + x;

        if (on) {
            buffer[index] |= (1 << bit);
        } else {
            buffer[index] &= ~(1 << bit);
        }
    }

    /**
     * Gets the current display buffer.
     *
     * @return Display buffer as byte array
     */
    public byte[] getBuffer() {
        return buffer.clone();
    }

    /**
     * Gets the display width.
     *
     * @return Display width in pixels
     */
    public int getWidth() {
        return WIDTH;
    }

    /**
     * Gets the display height.
     *
     * @return Display height in pixels
     */
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * Cleanup resources when done with the display.
     */
    public void cleanup() {
        if (spi != null) {
            spi.close();
        }
    }
}
