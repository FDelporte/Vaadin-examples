package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PageTitle("LCD Display")
@Route(value = "lcddisplay", layout = MainLayout.class)
public class LcdDisplayView extends VerticalLayout implements ChangeListener {
    private final Logger logger = LoggerFactory.getLogger(LcdDisplayView.class);

    private final Pi4JService pi4JService;
    private final LogGrid logs;

    public LcdDisplayView(Pi4JService pi4JService) {
        this.pi4JService = pi4JService;

        setMargin(true);

        var clear = new Button("Clear");
        clear.addClickListener(e -> pi4JService.clearLcdDisplay());

        TextField row1 = new TextField();
        row1.setLabel("Row 1");
        row1.setClearButtonVisible(true);
        row1.setMaxLength(16);

        var setRow1 = new Button("Set row 1");
        setRow1.addClickListener(e -> pi4JService.setLcdDisplay(1, row1.getValue()));

        TextField row2 = new TextField();
        row2.setLabel("Row 2");
        row2.setClearButtonVisible(true);
        row2.setMaxLength(16);

        var setRow2 = new Button("Set row 2");
        setRow2.addClickListener(e -> pi4JService.setLcdDisplay(2, row2.getValue()));

        logs = new LogGrid();
        add(clear, row1, setRow1, row2, setRow2, logs);
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
        if (!type.equals(ChangeType.LCD)) {
            return;
        }
        logger.debug("Message received: {}", message);
        logs.addLine(message);
    }
}
