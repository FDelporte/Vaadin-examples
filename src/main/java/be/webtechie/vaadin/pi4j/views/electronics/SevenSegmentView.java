package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentListener;
import be.webtechie.vaadin.pi4j.service.segment.SevenSegmentSymbol;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Seven Segment Display")
@Route(value = "sevensegment", layout = MainLayout.class)
public class SevenSegmentView extends VerticalLayout implements SevenSegmentListener {

    private final ListBox<Label> currentState;
    private final List<ComboBox<SevenSegmentSymbol>> comboboxes;

    public SevenSegmentView(@Autowired Pi4JService pi4JService) {
        comboboxes = new ArrayList<>();
        setMargin(true);

        var clear = new Button("Clear");
        clear.addClickListener(e -> pi4JService.ledMatrixClear());
        add(clear);

        var symbolHolder = new HorizontalLayout();
        add(symbolHolder);
        for (var i = 0; i < 4; i++) {
            var symbol = new ComboBox<SevenSegmentSymbol>();
            symbol.setItems(SevenSegmentSymbol.values());
            symbol.setItemLabelGenerator(SevenSegmentSymbol::getLabel);
            int finalI = i;
            symbol.addValueChangeListener(e -> {
                if (symbol.getValue() == null) {
                    pi4JService.setSevenSegmentDigit(finalI, SevenSegmentSymbol.EMPTY);
                } else {
                    pi4JService.setSevenSegmentDigit(finalI, symbol.getValue());
                }
            });
            symbolHolder.add(symbol);
            comboboxes.add(symbol);
        }

        currentState = new ListBox<>();
        pi4JService.addSevenSegmentListener(this);

        add(currentState);
    }

    @Override
    public void onSevenSegmentChange(int position, SevenSegmentSymbol symbol) {
        currentState.addComponentAsFirst(new Label(ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH.mm.ss"))
                + " - Position: " + (position + 1)
                + " - Symbol: " + symbol.name()));
        comboboxes.get(position).setValue(symbol);
    }
}
