package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import com.pi4j.io.gpio.digital.DigitalState;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
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
public class KeyPressView extends VerticalLayout implements ChangeListener {

    private final Logger logger = LoggerFactory.getLogger(KeyPressView.class);

    private final ComponentEventPublisher publisher;
    private final UI ui;
    private final H2 statusLabel;

    public KeyPressView(ComponentEventPublisher publisher) {
        this.publisher = publisher;

        setMargin(true);
        setSpacing(true);

        ui = UI.getCurrent();

        statusLabel = new H2("Waiting for key press...");
        statusLabel.getStyle().setColor("#666666");

        var infoLabel = new Paragraph("Press the key button on the board to see the response.");

        add(statusLabel, infoLabel);
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        publisher.addListener(this);
    }

    @Override
    public void onDetach(DetachEvent detachEvent) {
        publisher.removeListener(this);
    }

    @Override
    public <T> void onMessage(ChangeListener.ChangeType type, T message) {
        if (!type.equals(ChangeType.KEY) || !(message instanceof DigitalState)) {
            return;
        }

        var state = (DigitalState) message;
        // Key is pressed when pin goes LOW (pull-up resistor)
        var isPressed = state.equals(DigitalState.LOW);

        logger.debug("Key event in listener: {} - Is pressed: {}", state, isPressed);

        ui.access(() -> {
            statusLabel.setText(isPressed ? "KEY PRESSED" : "Key released");
            statusLabel.getStyle().setColor(isPressed ? "#009900" : "#666666");
            statusLabel.getStyle().setFontWeight(isPressed ? "bold" : "normal");
        });
    }
}
