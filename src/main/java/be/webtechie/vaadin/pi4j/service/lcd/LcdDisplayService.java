package be.webtechie.vaadin.pi4j.service.lcd;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.LcdDisplayView;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.character.hd44780.Hd44780Driver;
import com.pi4j.io.i2c.I2C;
import com.pi4j.plugin.ffm.common.HexFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LcdDisplayService {

    private static final Logger logger = LoggerFactory.getLogger(LcdDisplayService.class);
    private final ComponentEventPublisher eventPublisher;
    private Hd44780Driver lcdDisplay;

    public LcdDisplayService(Context pi4j, BoardConfig config, ComponentEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;

        if (!config.hasLcd() || config.getI2cDeviceLcd() == 0x00) {
            logger.info("LCD display not available on this board");
            return;
        }

        try {
            var bus = config.getI2cBus();
            var device = config.getI2cDeviceLcd();

            logger.info("Initializing LCD display on bus {} and device {}", bus, device);

            var i2c = pi4j.create(I2C.newConfigBuilder(pi4j)
                    .id("I2C-LCD-" + bus + "-" + HexFormatter.format(device))
                    .bus(bus)
                    .device((int) device)
                    .build());

            this.lcdDisplay = Hd44780Driver.withMcp23008Connection(i2c, 16, 2);
            this.lcdDisplay.writeAt(0, 0, "Hello");
            this.lcdDisplay.writeAt(0, 1, "   World!");

            // Register the view for this feature
            pi4JService.registerView(LcdDisplayView.class);

            logger.info("LCD display initialized");
        } catch (Exception e) {
            logger.error("Error initializing LCD display: {}", e.getMessage());
        }
    }

    public boolean isAvailable() {
        return lcdDisplay != null;
    }

    public void clear() {
        if (lcdDisplay == null) {
            logger.warn("LCD display not available");
            return;
        }
        lcdDisplay.clear();
    }

    public void setText(int row, String text) {
        if (lcdDisplay == null) {
            logger.warn("LCD display not available");
            return;
        }
        String paddedText = String.format("%-16s", text);
        if (paddedText.length() > 16) {
            paddedText = paddedText.substring(0, 16);
        }
        lcdDisplay.writeAt(0, row, paddedText);
        eventPublisher.publish(ChangeListener.ChangeType.LCD, "Set on row " + row + ": '" + text + "'");
    }
}
