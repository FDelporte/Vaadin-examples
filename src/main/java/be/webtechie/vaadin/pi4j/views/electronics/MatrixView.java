package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixDirection;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixSymbol;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

@PageTitle("8x8 LED Matrix")
@Route(value = "matrix", layout = MainLayout.class)
public class MatrixView extends VerticalLayout implements ChangeListener {
    private final Logger logger = LoggerFactory.getLogger(MatrixView.class);

    private final Pi4JService pi4JService;
    private final LogGrid logs;

    public MatrixView(Pi4JService pi4JService) {
        this.pi4JService = pi4JService;

        setMargin(true);

        var clear = new Button("Clear");
        clear.addClickListener(e -> pi4JService.clearLedMatrix());

        var symbols = new ComboBox<MatrixSymbol>();
        symbols.setItems(MatrixSymbol.values());
        symbols.setItemLabelGenerator(MatrixSymbol::name);
        symbols.addValueChangeListener(e -> {
            if (symbols.getValue() != null) {
                pi4JService.setLedMatrix(symbols.getValue());
            }
        });

        var rotateUp = new Button("Move", LineAwesomeIcon.ARROW_ALT_CIRCLE_UP_SOLID.create());
        rotateUp.addClickListener(e -> pi4JService.moveLedMatrix(MatrixDirection.UP));
        var rotateDown = new Button("Move", LineAwesomeIcon.ARROW_ALT_CIRCLE_DOWN_SOLID.create());
        rotateDown.addClickListener(e -> pi4JService.moveLedMatrix(MatrixDirection.DOWN));
        var rotateLeft = new Button("Move", LineAwesomeIcon.ARROW_ALT_CIRCLE_LEFT_SOLID.create());
        rotateLeft.addClickListener(e -> pi4JService.moveLedMatrix(MatrixDirection.LEFT));
        var rotateRight = new Button("Move", LineAwesomeIcon.ARROW_ALT_CIRCLE_RIGHT_SOLID.create());
        rotateRight.addClickListener(e -> pi4JService.moveLedMatrix(MatrixDirection.RIGHT));

        var rotateHolder = new HorizontalLayout(rotateUp, rotateDown, rotateLeft, rotateRight);

        logs = new LogGrid();
        add(clear, symbols, rotateHolder, logs);
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
        if (!type.equals(ChangeType.MATRIX)) {
            return;
        }
        logger.debug("Message received: {}", message);
        logs.addLine(message);
    }
}
