package be.webtechie.vaadin.pi4j.views.led;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("LED")
@Route(value = "LED", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class LEDView extends HorizontalLayout {

    public LEDView(@Autowired Pi4JService pi4JService) {
        var lbl = new Label("LED");
        var state = new Checkbox("Turn the LED on");
        state.addValueChangeListener(e -> pi4JService.setLedState(e.getValue()));

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, lbl, state);

        add(lbl, state);
    }

}
