package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.led.LedService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("LED")
@Menu(order = 10, icon = LineAwesomeIconUrl.LIGHTBULB_SOLID)
public class LEDView extends HardwareDemoView {

    public LEDView(LedService ledService, Pi4JService pi4JService) {
        var turnOnOff = new Checkbox("Turn the LED on");
        turnOnOff.addValueChangeListener(e -> ledService.setState(e.getValue()));
        add(turnOnOff);
    }
}
