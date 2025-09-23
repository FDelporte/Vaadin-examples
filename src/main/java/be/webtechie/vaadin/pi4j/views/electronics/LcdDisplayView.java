package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("LCD Display")
@Route("lcddisplay")
@Menu(order = 1, icon = LineAwesomeIconUrl.TABLE_SOLID)
public class LcdDisplayView extends VerticalLayout implements ChangeListener {
    private final Logger logger = LoggerFactory.getLogger(LcdDisplayView.class);

    private final Pi4JService pi4JService;
    private final LogGrid logs;

    public LcdDisplayView(Pi4JService pi4JService) {
        this.pi4JService = pi4JService;

        setMargin(true);

        var clear = new Button("Clear");
        clear.addClickListener(e -> pi4JService.clearLcdDisplay());

        logs = new LogGrid();
        add(clear, new TextRowControl(1), new TextRowControl(2), logs);
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
        if (!type.equals(ChangeType.LCD)) {
            return;
        }
        logger.debug("Message received: {}", message);
        logs.addLine((String) message);
    }

    private class TextRowControl extends HorizontalLayout {
        public TextRowControl(int counter) {
            var textInput = new TextField();
            textInput.setLabel("Row " + counter);
            textInput.setClearButtonVisible(true);
            textInput.setMaxLength(16);
            this.add(textInput);

            var sendButton = new Button("Update display");
            sendButton.addClickListener(e -> pi4JService.setLcdDisplay(counter, textInput.getValue()));
            this.add(sendButton);

            this.setAlignItems(Alignment.BASELINE);
        }
    }
}
