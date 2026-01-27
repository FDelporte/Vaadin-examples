package be.webtechie.vaadin.pi4j.service.oled;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.DisplayEvent;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.OledDisplayView;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Service for controlling SSD1306 OLED display on boards that support it (e.g., Pioneer600).
 */
@Service
public class OledService {

    private static final Logger logger = LoggerFactory.getLogger(OledService.class);

    private final SSD1306 oledDisplay;
    private final ApplicationEventPublisher eventPublisher;
    private boolean dimmed = false;

    public OledService(Context pi4j, BoardConfig config, ApplicationEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;

        if (!config.hasOled() || config.getOledDcPin() < 0 || config.getOledRstPin() < 0) {
            logger.info("OLED display not available on this board");
            this.oledDisplay = null;
            return;
        }

        try {
            this.oledDisplay = new SSD1306(pi4j,
                    config.getOledSpiChannel(),
                    8000000, // 8MHz SPI baud rate
                    config.getOledDcPin(),
                    config.getOledRstPin());
            this.oledDisplay.begin();
            this.oledDisplay.clear();
            this.oledDisplay.display();

            // Register the view for this feature
            pi4JService.registerView(OledDisplayView.class);

            logger.info("OLED display initialized on DC pin {}, RST pin {}",
                    config.getOledDcPin(), config.getOledRstPin());
        } catch (Exception e) {
            logger.error("Error initializing OLED display: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize OLED display", e);
        }
    }

    /**
     * Returns true if the OLED display is available on this board.
     */
    public boolean isAvailable() {
        return oledDisplay != null;
    }

    /**
     * Clears the OLED display.
     */
    public void clear() {
        if (oledDisplay == null) {
            logger.warn("OLED display not available");
            return;
        }
        logger.info("Clearing OLED display");
        oledDisplay.clear();
        oledDisplay.display();
        eventPublisher.publishEvent(new DisplayEvent(this, DisplayEvent.DisplayType.OLED, "Display cleared"));
    }

    /**
     * Displays text on the OLED display.
     */
    public void displayText(String text) {
        if (oledDisplay == null) {
            logger.warn("OLED display not available");
            return;
        }
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        logger.info("Displaying text on OLED: '{}'", text);

        // Create a 128x64 image and draw text on it
        BufferedImage image = new BufferedImage(SSD1306.WIDTH, SSD1306.HEIGHT, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        // Draw text, handling multiple lines if needed
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length && i < 5; i++) { // Max 5 lines for 64px height
            g2d.drawString(lines[i], 2, 15 + i * 13);
        }
        g2d.dispose();

        oledDisplay.image(image);
        oledDisplay.display();
        eventPublisher.publishEvent(new DisplayEvent(this, DisplayEvent.DisplayType.OLED, "Text displayed: " + text));
    }

    /**
     * Tests the OLED display with shapes and text.
     */
    public void testDisplay() {
        if (oledDisplay == null) {
            logger.warn("OLED display not available");
            return;
        }

        logger.info("Testing OLED display with shapes and text");

        // Create a 128x64 image for drawing
        BufferedImage image = new BufferedImage(SSD1306.WIDTH, SSD1306.HEIGHT, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D draw = image.createGraphics();
        draw.setColor(Color.WHITE);

        // Clear background
        draw.setColor(Color.BLACK);
        draw.fillRect(0, 0, SSD1306.WIDTH, SSD1306.HEIGHT);
        draw.setColor(Color.WHITE);

        // Draw shapes
        int padding = 2;
        int shapeWidth = 20;
        int top = padding;
        int bottom = SSD1306.HEIGHT - padding;
        int x = padding;

        // Draw an ellipse
        draw.drawOval(x, top, shapeWidth, bottom - top);
        x += shapeWidth + padding;

        // Draw a rectangle
        draw.drawRect(x, top, shapeWidth, bottom - top);
        x += shapeWidth + padding;

        // Draw a triangle
        int[] xPoints = {x, x + shapeWidth / 2, x + shapeWidth};
        int[] yPoints = {bottom, top, bottom};
        draw.drawPolygon(xPoints, yPoints, 3);
        x += shapeWidth + padding;

        // Draw an X
        draw.drawLine(x, bottom, x + shapeWidth, top);
        draw.drawLine(x, top, x + shapeWidth, bottom);
        x += shapeWidth + padding;

        // Write text
        draw.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        draw.drawString("Hello", x, top + 15);
        draw.drawString("World!", x, top + 30);

        draw.dispose();

        oledDisplay.image(image);
        oledDisplay.display();
        eventPublisher.publishEvent(new DisplayEvent(this, DisplayEvent.DisplayType.OLED, "Test display completed with shapes and text"));
    }

    /**
     * Sets the OLED display contrast.
     *
     * @param contrast Contrast value (0-255)
     */
    public void setContrast(int contrast) {
        if (oledDisplay == null) {
            logger.warn("OLED display not available");
            return;
        }
        logger.info("Setting OLED contrast to {}", contrast);
        oledDisplay.setContrast(contrast);
        eventPublisher.publishEvent(new DisplayEvent(this, DisplayEvent.DisplayType.OLED, "Contrast set to: " + contrast));
    }

    /**
     * Toggles the OLED display dim state.
     */
    public void toggleDim() {
        if (oledDisplay == null) {
            logger.warn("OLED display not available");
            return;
        }
        logger.info("Toggling OLED dim state");
        dimmed = !dimmed;
        oledDisplay.dim(dimmed);
        eventPublisher.publishEvent(new DisplayEvent(this, DisplayEvent.DisplayType.OLED, "Dim toggled to: " + (dimmed ? "ON" : "OFF")));
    }
}
