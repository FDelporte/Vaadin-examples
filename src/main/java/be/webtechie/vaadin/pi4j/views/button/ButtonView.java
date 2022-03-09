package be.webtechie.vaadin.pi4j.views.button;

import be.webtechie.vaadin.pi4j.service.ButtonListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.pi4j.io.gpio.digital.DigitalState;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Button")
@Route(value = "Button", layout = MainLayout.class)
public class ButtonView extends HorizontalLayout implements ButtonListener {

    private final Checkbox checkbox;

    public ButtonView(@Autowired Pi4JService pi4JService) {
        checkbox = new Checkbox();
        checkbox.setLabel("Button is pressed");
        checkbox.setEnabled(false);
        add(checkbox);

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, checkbox);

        pi4JService.addButtonListener(this);
    }

    @Override
    public void onButtonEvent(DigitalState state) {
        checkbox.setValue(state == DigitalState.HIGH);
    }
}
