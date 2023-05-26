package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.touch.TouchListener;
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

@PageTitle("Touch")
@Route(value = "touch", layout = MainLayout.class)
public class TouchView extends HorizontalLayout implements TouchListener {

    private final UI ui;
    private final Label lbl;
    Logger logger = LoggerFactory.getLogger(TouchView.class);

    public TouchView(@Autowired Pi4JService pi4JService) {
        ui = UI.getCurrent();
        lbl = new Label("Waiting for touch event...");
        add(lbl);

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, lbl);

        pi4JService.addButtonListener(this);
    }

    @Override
    public void onTouchEvent(DigitalState state) {
        var isPressed = state.equals(DigitalState.HIGH);
        logger.info("Touch event in listener: {} - Is on: {}", state, isPressed);
        ui.accessSynchronously(() -> lbl.setText(isPressed ? "Touch sensor is pressed" : "Touch sensor is released"));
    }
}
