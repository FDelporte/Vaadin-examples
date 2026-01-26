package be.webtechie.vaadin.pi4j.service.matrix;

import be.webtechie.vaadin.pi4j.config.CrowPiConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.RedMatrixView;
import be.webtechie.vaadin.pi4j.views.electronics.RgbMatrixView;
import com.pi4j.context.Context;
import com.vaadin.flow.router.RouteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class RedMatrixService {

    private static final Logger logger = LoggerFactory.getLogger(RedMatrixService.class);
    private final boolean isRgbMatrix;
    private final LedMatrixComponent ledMatrixComponent;
    private final RgbMatrixService rgbMatrixService;
    private final ComponentEventPublisher eventPublisher;

    public RedMatrixService(Context pi4j, CrowPiConfig config, ComponentEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;
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

    public void clear() {
        if (isRgbMatrix) {
            rgbMatrixService.clear();
            rgbMatrixService.refresh();
        } else {
            ledMatrixComponent.clear();
            ledMatrixComponent.refresh();
        }
    }

    public void setSymbol(MatrixSymbol symbol) {
        logger.info("LED matrix print: {}", symbol.name());
        if (isRgbMatrix) {
            rgbMatrixService.print(symbol, Color.BLUE);
        } else {
            ledMatrixComponent.print(symbol);
        }
        eventPublisher.publish(ChangeListener.ChangeType.MATRIX,
                "Symbol: " + symbol.name() + " - HEX: " + symbol.getHexValue());
    }

    public void move(MatrixDirection direction) {
        logger.info("LED matrix rotate: {}", direction.name());
        if (isRgbMatrix) {
            rgbMatrixService.rotate(direction);
        } else {
            ledMatrixComponent.rotate(direction);
        }
        eventPublisher.publish(ChangeListener.ChangeType.MATRIX, "Move: " + direction.name());
    }
}