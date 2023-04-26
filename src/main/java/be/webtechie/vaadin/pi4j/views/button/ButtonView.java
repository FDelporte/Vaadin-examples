package be.webtechie.vaadin.pi4j.views.button;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.button.ButtonListener;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.pi4j.io.gpio.digital.DigitalState;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Button")
@Route(value = "button", layout = MainLayout.class)
public class ButtonView extends HorizontalLayout implements ButtonListener {

    private final UI ui;
    private final Label lbl;
    Logger logger = LoggerFactory.getLogger(ButtonView.class);

    public ButtonView(@Autowired Pi4JService pi4JService) {
        ui = UI.getCurrent();
        lbl = new Label("Waiting for button change...");
        add(lbl);

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, lbl);

        pi4JService.addButtonListener(this);
    }

    @Override
    public void onButtonEvent(DigitalState state) {
        var isPressed = state.equals(DigitalState.HIGH);
        logger.info("Button event in listener: {} - Is on: {}", state, isPressed);
        ui.accessSynchronously(() -> lbl.setText(isPressed ? "Button is pressed" : "Button is released"));
    }
}
