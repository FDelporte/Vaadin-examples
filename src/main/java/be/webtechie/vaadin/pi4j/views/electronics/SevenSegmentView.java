package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
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
@Route("sevensegment")
@Menu(order = 1, icon = LineAwesomeIconUrl.TABLE_SOLID)
public class SevenSegmentView extends VerticalLayout implements ChangeListener {
    private final Logger logger = LoggerFactory.getLogger(SevenSegmentView.class);

    private final Pi4JService pi4JService;
    private final LogGrid logs;

    public SevenSegmentView(Pi4JService pi4JService) {
        this.pi4JService = pi4JService;

        setMargin(true);

        var clear = new Button("Clear");
        clear.addClickListener(e -> pi4JService.clearSevenSegment());
        add(clear);

        var symbolHolder = new HorizontalLayout();
        add(symbolHolder);
        for (var i = 0; i < 4; i++) {
            var symbol = new ComboBox<SevenSegmentSymbol>();
            symbol.setItems(SevenSegmentSymbol.values());
            symbol.setItemLabelGenerator(SevenSegmentSymbol::getLabel);
            symbol.setWidth(75, Unit.PIXELS);
            int finalI = i;
            symbol.addValueChangeListener(e -> {
                if (symbol.getValue() == null) {
                    pi4JService.setSevenSegment(finalI, SevenSegmentSymbol.EMPTY);
                } else {
                    pi4JService.setSevenSegment(finalI, symbol.getValue());
                }
            });
            symbolHolder.add(symbol);
        }

        logs = new LogGrid();
        add(logs);
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        pi4JService.addListener(this);
    }

    @Override
    public void onDetach(DetachEvent detachEvent) {
        pi4JService.removeListener(this);
    }

    @Override
    public void onMessage(ChangeListener.ChangeType type, String message) {
        if (!type.equals(ChangeType.SEGMENT)) {
            return;
        }
        logger.debug("Message received: {}", message);
        logs.addLine(message);
    }
}
