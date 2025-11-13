package be.webtechie.vaadin.pi4j.service.segment
;

import be.webtechie.vaadin.pi4j.config.CrowPiConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SevenSegmentService {

    private static final Logger logger = LoggerFactory.getLogger(SevenSegmentService.class);
    private final ComponentEventPublisher eventPublisher;
    private SevenSegmentComponent component;

    public SevenSegmentService(Context pi4j, CrowPiConfig config, ComponentEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        try {
            this.component = new SevenSegmentComponent(pi4j, config.getI2cBus(), config.getI2cDeviceSevenSegmentDisplay(), config.getSevenSegmentDisplayIndexes());
            this.component.setEnabled(true);
            this.component.setBlinkRate(0);
            this.component.setBrightness(15);
            this.component.clear();
            setSymbol(0, SevenSegmentSymbol.NUMBER_0);
            setSymbol(1, SevenSegmentSymbol.NUMBER_0);
            setSymbol(2, SevenSegmentSymbol.NUMBER_0);
            setSymbol(3, SevenSegmentSymbol.NUMBER_0);
            logger.info("Seven segment display initialized");
        } catch (Exception e) {
            logger.error("Error initializing seven segment display: {}", e.getMessage());
        }
    }

    public void setSymbol(int position, SevenSegmentSymbol symbol) {
        if (component == null) {
            logger.error("Seven segment display not initialized");
            return;
        }
        logger.info("Setting digit {} on position {} of seven segment display", symbol.name(), position);
        component.setSymbol(position, symbol);
        component.refresh();
        eventPublisher.publish(ChangeListener.ChangeType.SEGMENT,
                "Position: " + (position + 1) + " - Symbol: " + symbol.name() +
                        " - HEX: " + symbol.getHexValue() + " - Bits: " + symbol.getBitsValue());
    }

    public void clear() {
        if (component == null) {
            logger.error("Seven segment display not initialized");
            return;
        }
        component.clear();
        component.refresh();
    }
}