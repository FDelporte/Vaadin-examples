package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixDirection;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixListener;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixSymbol;
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
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@PageTitle("8x8 LED Matrix")
@Route(value = "matrix", layout = MainLayout.class)
public class MatrixView extends VerticalLayout implements MatrixListener {

    private final ListBox<Label> currentState;

    public MatrixView(@Autowired Pi4JService pi4JService) {
        setMargin(true);

        var clear = new Button("Clear");
        clear.addClickListener(e -> pi4JService.ledMatrixClear());

        var symbols = new ComboBox<MatrixSymbol>();
        symbols.setItems(MatrixSymbol.values());
        symbols.setItemLabelGenerator(MatrixSymbol::name);
        symbols.addValueChangeListener(e -> {
            if (symbols.getValue() != null) {
                pi4JService.ledMatrixPrint(symbols.getValue());
            }
        });

        var rotateUp = new Button("Move", LineAwesomeIcon.ARROW_ALT_CIRCLE_UP_SOLID.create());
        rotateUp.addClickListener(e -> pi4JService.ledMatrixMove(MatrixDirection.UP));
        var rotateDown = new Button("Move", LineAwesomeIcon.ARROW_ALT_CIRCLE_DOWN_SOLID.create());
        rotateDown.addClickListener(e -> pi4JService.ledMatrixMove(MatrixDirection.DOWN));
        var rotateLeft = new Button("Move", LineAwesomeIcon.ARROW_ALT_CIRCLE_LEFT_SOLID.create());
        rotateLeft.addClickListener(e -> pi4JService.ledMatrixMove(MatrixDirection.LEFT));
        var rotateRight = new Button("Move", LineAwesomeIcon.ARROW_ALT_CIRCLE_RIGHT_SOLID.create());
        rotateRight.addClickListener(e -> pi4JService.ledMatrixMove(MatrixDirection.RIGHT));

        var rotateHolder = new HorizontalLayout(rotateUp, rotateDown, rotateLeft, rotateRight);

        currentState = new ListBox<>();
        pi4JService.addMatrixListener(this);

        add(clear, symbols, rotateHolder, currentState);
    }

    @Override
    public void onMatrixChange(MatrixSymbol symbol, MatrixDirection direction) {
        currentState.addComponentAsFirst(new Label(ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH.mm.ss"))
                + " - Symbol: " + symbol.name()
                + " - Direction: " + direction.name()));
    }
}
