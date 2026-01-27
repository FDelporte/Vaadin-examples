package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.DisplayEvent;
import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.service.lcd.LcdDisplayService;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("LCD Display")
// @Route("lcddisplay") - Conditionally registered by LcdDisplayService
@Menu(order = 13, icon = LineAwesomeIconUrl.TABLE_SOLID)
public class LcdDisplayView extends VerticalLayout {
    private final Logger logger = LoggerFactory.getLogger(LcdDisplayView.class);

    private final LcdDisplayService lcdDisplayService;
    private final LogGrid logs;

    public LcdDisplayView(ComponentEventBus eventBus, LcdDisplayService lcdDisplayService) {
        this.lcdDisplayService = lcdDisplayService;

        eventBus.subscribe(this, DisplayEvent.class, this::onDisplayEvent);

        setMargin(true);

        var clear = new Button("Clear");
        clear.addClickListener(e -> lcdDisplayService.clear());

        logs = new LogGrid();
        add(clear, new TextRowControl(0), new TextRowControl(1), logs);
    }

    private void onDisplayEvent(DisplayEvent event) {
        if (event.getDisplayType() != DisplayEvent.DisplayType.LCD) {
            return;
        }
        logger.debug("Message received: {}", event.getMessage());
        logs.addLine(event.getMessage());
    }

    private class TextRowControl extends HorizontalLayout {
        public TextRowControl(int counter) {
            var textInput = new TextField();
            textInput.setLabel("Row " + (counter + 1));
            textInput.setClearButtonVisible(true);
            textInput.setMaxLength(16);
            this.add(textInput);

            var sendButton = new Button("Update display");
            sendButton.addClickListener(e -> lcdDisplayService.setText(counter, textInput.getValue()));
            this.add(sendButton);

            this.setAlignItems(Alignment.BASELINE);
        }
    }
}
