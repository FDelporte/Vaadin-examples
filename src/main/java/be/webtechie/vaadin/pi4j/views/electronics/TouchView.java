package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.event.TouchStateEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Touch")
// @Route("touch") - Conditionally registered by TouchSensorService
@Menu(order = 11, icon = LineAwesomeIconUrl.POWER_OFF_SOLID)
public class TouchView extends HorizontalLayout {

    private final H2 lbl;
    private final Logger logger = LoggerFactory.getLogger(TouchView.class);

    public TouchView(ComponentEventBus eventBus) {
        eventBus.subscribe(this, TouchStateEvent.class, this::onTouchEvent);

        setMargin(true);

        lbl = new H2("Waiting for touch event...");
        add(lbl);
    }

    private void onTouchEvent(TouchStateEvent event) {
        var isTouched = event.isTouched();
        logger.debug("Touch event in listener: {} - Is on: {}", event.getState(), isTouched);

        lbl.setText(isTouched ? "Touch sensor is pressed" : "Touch sensor is released");
        lbl.getStyle().setColor(isTouched ? "#009900" : "#990000");
        lbl.getStyle().setBackgroundColor(isTouched ? "#FFFFFF" : "#999999");
    }
}
