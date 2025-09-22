package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import com.pi4j.io.gpio.digital.DigitalState;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Touch")
@Route("touch")
@Menu(order = 1, icon = LineAwesomeIconUrl.POWER_OFF_SOLID)
public class TouchView extends HorizontalLayout implements ChangeListener {

    private final Pi4JService pi4JService;
    private final UI ui;
    private final H2 lbl;
    Logger logger = LoggerFactory.getLogger(TouchView.class);

    public TouchView(Pi4JService pi4JService) {
        this.pi4JService = pi4JService;

        setMargin(true);

        ui = UI.getCurrent();
        lbl = new H2("Waiting for touch event...");
        add(lbl);
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
        if (!type.equals(ChangeType.TOUCH) && !(message instanceof DigitalState)) {
            return;
        }
        var state = (DigitalState) message;
        var isPressed = state.equals(DigitalState.HIGH);
        logger.debug("Touch event in listener: {} - Is on: {}", state, isPressed);
        ui.accessSynchronously(() -> {
            lbl.setText(isPressed ? "Touch sensor is pressed" : "Touch sensor is released");
            lbl.getStyle().set("color", isPressed ? "#009900" : "#990000");
            lbl.getStyle().set("background-color", isPressed ? "#FFFFFF" : "#999999");
        });
    }
}
