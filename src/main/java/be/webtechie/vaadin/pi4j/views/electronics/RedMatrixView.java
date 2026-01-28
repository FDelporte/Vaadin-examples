package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.DisplayEvent;
import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixDirection;
import be.webtechie.vaadin.pi4j.service.matrix.MatrixSymbol;
import be.webtechie.vaadin.pi4j.service.matrix.RedMatrixService;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("8x8 Red LED Matrix")
@Menu(order = 14, icon = LineAwesomeIconUrl.TABLE_SOLID)
public class RedMatrixView extends HardwareDemoView {
    private final Logger logger = LoggerFactory.getLogger(RedMatrixView.class);

    private final RedMatrixService redMatrixService;
    private final LogGrid logs;

    public RedMatrixView(ComponentEventBus eventBus, RedMatrixService redMatrixService) {
        this.redMatrixService = redMatrixService;

        eventBus.subscribe(this, DisplayEvent.class, this::onDisplayEvent);

        var clear = new Button("Clear");
        clear.addClickListener(e -> redMatrixService.clear());

        var symbols = new ComboBox<MatrixSymbol>();
        symbols.setItems(MatrixSymbol.values());
        symbols.setItemLabelGenerator(MatrixSymbol::name);
        symbols.addValueChangeListener(e -> {
            if (symbols.getValue() != null) {
                redMatrixService.setSymbol(symbols.getValue());
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

    private void onDisplayEvent(DisplayEvent event) {
        if (event.getDisplayType() != DisplayEvent.DisplayType.MATRIX) {
            return;
        }
        logger.debug("Message received: {}", event.getMessage());
        logs.addLine(event.getMessage());
    }

    private class MoveButton extends Button {
        public MoveButton(VaadinIcon icon, MatrixDirection direction) {
            this.setText("Move");
            this.setIcon(icon.create());
            this.addClickListener(e -> redMatrixService.move(direction));
        }
    }
}
