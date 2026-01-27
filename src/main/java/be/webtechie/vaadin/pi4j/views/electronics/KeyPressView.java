package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.event.KeyStateEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * View for displaying key press events from boards that support it (e.g., Pioneer600).
 * This view is dynamically registered by KeyService when the board has key support.
 */
@PageTitle("Key Press")
// @Route("keypressview") - Conditionally registered in KeyService
@Menu(order = 12, icon = LineAwesomeIconUrl.KEYBOARD_SOLID)
public class KeyPressView extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(KeyPressView.class);
    private final H2 statusLabel;

    public KeyPressView(ComponentEventBus eventBus) {
        eventBus.subscribe(this, KeyStateEvent.class, this::onKeyEvent);

        setMargin(true);
        setSpacing(true);

        statusLabel = new H2("Waiting for key press...");
        statusLabel.getStyle().setColor("#666666");

        var infoLabel = new Paragraph("Press the key button on the board to see the response.");

        add(statusLabel, infoLabel);
    }

    private void onKeyEvent(KeyStateEvent event) {
        var isPressed = event.isPressed();
        logger.debug("Key event in listener: {} - Is pressed: {}", event.getState(), isPressed);

        statusLabel.setText(isPressed ? "KEY PRESSED" : "Key released");
        statusLabel.getStyle().setColor(isPressed ? "#009900" : "#666666");
        statusLabel.getStyle().setFontWeight(isPressed ? "bold" : "normal");
    }
}
