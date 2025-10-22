package be.webtechie.vaadin.pi4j.service.lcd;

import be.webtechie.vaadin.pi4j.config.CrowPiConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.character.hd44780.Hd44780Driver;
import com.pi4j.io.i2c.I2C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LcdDisplayService {

    private static final Logger logger = LoggerFactory.getLogger(LcdDisplayService.class);
    private final ComponentEventPublisher eventPublisher;
    private Hd44780Driver lcdDisplay;

    public LcdDisplayService(Context pi4j, CrowPiConfig config, ComponentEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        try {
            var i2cBus = config.getI2cBus();
            var i2cDevice = config.getI2cDeviceLcd();

            logger.info("Initializing LCD display on bus {} and device {}", i2cBus, i2cDevice);

            var i2c = pi4j.create(I2C.newConfigBuilder(pi4j)
                    .bus(i2cBus)
                    .device(i2cDevice)
                    .build());

            this.lcdDisplay = Hd44780Driver.withMcp23008Connection(i2c, 16, 2);
            this.lcdDisplay.writeAt(0, 0, "Hello");
            this.lcdDisplay.writeAt(0, 1, "   World!");

            logger.info("LCD display initialized");
        } catch (Exception e) {
            logger.error("Error initializing LCD display: {}", e.getMessage());
        }
    }

    public void clear() {
        lcdDisplay.clearDisplay();
    }

    public void setText(int row, String text) {
        String paddedText = String.format("%-16s", text);
        if (paddedText.length() > 16) {
            paddedText = paddedText.substring(0, 16);
        }
        lcdDisplay.writeAt(0, row, paddedText);
        eventPublisher.publish(ChangeListener.ChangeType.LCD, "Set on row " + row + ": '" + text + "'");
    }
}