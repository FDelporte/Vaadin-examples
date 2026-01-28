package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.DisplayEvent;
import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentService;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentSymbol;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Seven Segment Display")
@Menu(order = 15, icon = LineAwesomeIconUrl.TABLE_SOLID)
public class SevenSegmentView extends HardwareDemoView {
    private final Logger logger = LoggerFactory.getLogger(SevenSegmentView.class);

    private final SevenSegmentService sevenSegmentService;
    private final LogGrid logs;

    public SevenSegmentView(ComponentEventBus eventBus, SevenSegmentService sevenSegmentService) {
        this.sevenSegmentService = sevenSegmentService;

        eventBus.subscribe(this, DisplayEvent.class, this::onDisplayEvent);

        var clear = new Button("Clear");
        clear.addClickListener(e -> sevenSegmentService.clear());
        add(clear);

        var symbolHolder = new HorizontalLayout();
        add(symbolHolder);
        for (var i = 0; i < 4; i++) {
            symbolHolder.add(new SymbolSelection(i));
        }

        logs = new LogGrid();
        add(logs);
    }

    private void onDisplayEvent(DisplayEvent event) {
        if (event.getDisplayType() != DisplayEvent.DisplayType.SEGMENT) {
            return;
        }
        logger.debug("Message received: {}", event.getMessage());
        logs.addLine(event.getMessage());
    }

    private class SymbolSelection extends ComboBox<SevenSegmentSymbol> {
        public SymbolSelection(int i) {
            this.setItems(SevenSegmentSymbol.values());
            this.setItemLabelGenerator(SevenSegmentSymbol::getLabel);
            this.setWidth(75, Unit.PIXELS);
            this.addValueChangeListener(e -> {
                if (this.getValue() == null) {
                    sevenSegmentService.setSymbol(i, SevenSegmentSymbol.EMPTY);
                } else {
                    sevenSegmentService.setSymbol(i, this.getValue());
                }
            });
        }
    }
}
