package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentService;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentSymbol;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Seven Segment Display")
// @Route("sevensegment") - Conditionally registered by SevenSegmentService
@Menu(order = 15, icon = LineAwesomeIconUrl.TABLE_SOLID)
public class SevenSegmentView extends VerticalLayout implements ChangeListener {
    private final Logger logger = LoggerFactory.getLogger(SevenSegmentView.class);

    private final ComponentEventPublisher publisher;
    private final SevenSegmentService sevenSegmentService;
    private final LogGrid logs;

    public SevenSegmentView(ComponentEventPublisher publisher, SevenSegmentService sevenSegmentService) {
        this.publisher = publisher;
        this.sevenSegmentService = sevenSegmentService;

        setMargin(true);

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

    @Override
    public void onAttach(AttachEvent attachEvent) {
        publisher.addListener(this);
    }

    @Override
    public void onDetach(DetachEvent detachEvent) {
        publisher.removeListener(this);
    }

    @Override
    public <T> void onMessage(ChangeListener.ChangeType type, T message) {
        if (!type.equals(ChangeType.SEGMENT)) {
            return;
        }
        logger.debug("Message received: {}", message);
        logs.addLine((String) message);
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
