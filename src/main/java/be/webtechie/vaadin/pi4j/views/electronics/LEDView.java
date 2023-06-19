package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("LED")
@Route(value = "led", layout = MainLayout.class)
public class LEDView extends HorizontalLayout {

    public LEDView(@Autowired Pi4JService pi4JService) {
        setMargin(true);
        
        var turnOnOff = new Checkbox("Turn the LED on");
        turnOnOff.addValueChangeListener(e -> pi4JService.setLedState(e.getValue()));
        add(turnOnOff);
    }
}
