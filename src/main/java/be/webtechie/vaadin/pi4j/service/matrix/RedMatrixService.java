package be.webtechie.vaadin.pi4j.service.matrix;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.DisplayEvent;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.RedMatrixView;
import be.webtechie.vaadin.pi4j.views.electronics.RgbMatrixView;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class RedMatrixService {

    private static final Logger logger = LoggerFactory.getLogger(RedMatrixService.class);
    private final boolean isRgbMatrix;
    private final LedMatrixComponent ledMatrixComponent;
    private final RgbMatrixService rgbMatrixService;
    private final ApplicationEventPublisher eventPublisher;

    public RedMatrixService(Context pi4j, BoardConfig config, ApplicationEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;

        // Check if the board has any matrix support
        if (!config.hasRGBMatrix() && !config.hasRedMatrix()) {
            logger.info("LED matrix not available on this board");
            this.isRgbMatrix = false;
            this.rgbMatrixService = null;
            this.ledMatrixComponent = null;
            return;
        }

        this.isRgbMatrix = config.hasRGBMatrix();

        if (isRgbMatrix) {
            this.rgbMatrixService = new RgbMatrixService(pi4j, config);
            this.ledMatrixComponent = null;
            logger.info("RGB LED matrix initialized");
            pi4JService.registerView(RgbMatrixView.class);
        } else {
            this.ledMatrixComponent = new LedMatrixComponent(pi4j);
            this.ledMatrixComponent.setEnabled(true);
            this.ledMatrixComponent.setBrightness(7);
            this.ledMatrixComponent.clear();
            this.rgbMatrixService = null;
            pi4JService.registerView(RedMatrixView.class);
            logger.info("LED matrix initialized");
        }
    }

    public boolean isAvailable() {
        return ledMatrixComponent != null || rgbMatrixService != null;
    }

    public void clear() {
        if (!isAvailable()) {
            logger.warn("LED matrix not available");
            return;
        }
        if (isRgbMatrix) {
            rgbMatrixService.clear();
            rgbMatrixService.refresh();
        } else {
            ledMatrixComponent.clear();
            ledMatrixComponent.refresh();
        }
    }

    public void setSymbol(MatrixSymbol symbol) {
        if (!isAvailable()) {
            logger.warn("LED matrix not available");
            return;
        }
        logger.info("LED matrix print: {}", symbol.name());
        if (isRgbMatrix) {
            rgbMatrixService.print(symbol, Color.BLUE);
        } else {
            ledMatrixComponent.print(symbol);
        }
        eventPublisher.publishEvent(new DisplayEvent(this, DisplayEvent.DisplayType.MATRIX,
                "Symbol: " + symbol.name() + " - HEX: " + symbol.getHexValue()));
    }

    public void move(MatrixDirection direction) {
        if (!isAvailable()) {
            logger.warn("LED matrix not available");
            return;
        }
        logger.info("LED matrix rotate: {}", direction.name());
        if (isRgbMatrix) {
            rgbMatrixService.rotate(direction);
        } else {
            ledMatrixComponent.rotate(direction);
        }
        eventPublisher.publishEvent(new DisplayEvent(this, DisplayEvent.DisplayType.MATRIX, "Move: " + direction.name()));
    }
}
