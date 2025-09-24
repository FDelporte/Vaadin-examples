package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixDirection;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixSymbol;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("8x8 LED Matrix")
@Route("matrix")
@Menu(order = 14, icon = LineAwesomeIconUrl.TABLE_SOLID)
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

        var rotateHolder = new HorizontalLayout(
                new MoveButton(VaadinIcon.ARROW_UP, MatrixDirection.UP),
                new MoveButton(VaadinIcon.ARROW_DOWN, MatrixDirection.DOWN),
                new MoveButton(VaadinIcon.ARROW_LEFT, MatrixDirection.LEFT),
                new MoveButton(VaadinIcon.ARROW_RIGHT, MatrixDirection.RIGHT)
        );

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
    public <T> void onMessage(ChangeListener.ChangeType type, T message) {
        if (!type.equals(ChangeType.MATRIX)) {
            return;
        }
        logger.debug("Message received: {}", message);
        logs.addLine((String) message);
    }

    private class MoveButton extends Button {
        public MoveButton(VaadinIcon icon, MatrixDirection direction) {
            this.setText("Move");
            this.setIcon(icon.create());
            this.addClickListener(e -> pi4JService.moveLedMatrix(direction));
        }
    }
}
