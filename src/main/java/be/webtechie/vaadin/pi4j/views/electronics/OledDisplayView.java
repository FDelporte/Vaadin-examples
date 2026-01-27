package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.DisplayEvent;
import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.service.oled.OledService;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * View for controlling the OLED display on boards that support it (e.g., Pioneer600).
 * This view is dynamically registered by OledService when the board has OLED support.
 */
@PageTitle("OLED Display")
// @Route("oleddisplayview") - Conditionally registered in OledService
@Menu(order = 13, icon = LineAwesomeIconUrl.DESKTOP_SOLID)
public class OledDisplayView extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(OledDisplayView.class);

    private final OledService oledService;
    private final LogGrid logs;

    public OledDisplayView(ComponentEventBus eventBus, OledService oledService) {
        this.oledService = oledService;

        eventBus.subscribe(this, DisplayEvent.class, this::onDisplayEvent);

        setMargin(true);

        // Clear display button
        var clear = new Button("Clear Display");
        clear.addClickListener(e -> oledService.clear());

        // Test display with shapes and text
        var testDisplay = new Button("Test Display");
        testDisplay.addClickListener(e -> oledService.testDisplay());

        // Text input and display
        TextArea textField = new TextArea();
        textField.setLabel("Text to display");
        textField.setClearButtonVisible(true);
        textField.setPlaceholder("Enter text to display");

        var displayText = new Button("Display Text");
        displayText.addClickListener(e -> oledService.displayText(textField.getValue()));

        // Contrast control
        TextField contrastField = new TextField();
        contrastField.setLabel("Contrast (0-255)");
        contrastField.setValue("207");
        contrastField.setPattern("[0-9]*");

        var setContrast = new Button("Set Contrast");
        setContrast.addClickListener(e -> {
            try {
                int contrast = Integer.parseInt(contrastField.getValue());
                oledService.setContrast(contrast);
            } catch (NumberFormatException ex) {
                logger.error("Invalid contrast value: {}", contrastField.getValue());
            }
        });

        var contrastLayout = new HorizontalLayout(contrastField, setContrast);
        contrastLayout.setAlignItems(Alignment.BASELINE);

        // Dim toggle
        var dimToggle = new Button("Toggle Dim");
        dimToggle.addClickListener(e -> oledService.toggleDim());

        logs = new LogGrid();

        add(clear, testDisplay, textField, displayText, contrastLayout, dimToggle, logs);
    }

    private void onDisplayEvent(DisplayEvent event) {
        if (event.getDisplayType() != DisplayEvent.DisplayType.OLED) {
            return;
        }
        logger.debug("OLED message received: {}", event.getMessage());
        logs.addLine(event.getMessage());
    }
}
