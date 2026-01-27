package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.joystick.JoystickService;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * View for triggering the PCF8574-based buzzer on Pioneer600.
 * This view is dynamically registered by JoystickService when the board has buzzer support.
 */
@PageTitle("Buzzer")
// @Route("simplebuzzer") - Conditionally registered by JoystickService
@Menu(order = 19, icon = LineAwesomeIconUrl.BELL_SOLID)
public class SimpleBuzzerView extends VerticalLayout {

    private final JoystickService joystickService;
    private final LogGrid logs;

    public SimpleBuzzerView(JoystickService joystickService) {
        this.joystickService = joystickService;

        setMargin(true);
        setSpacing(true);

        add(new H3("Buzzer (PCF8574)"));
        add(new Paragraph("Click the button to trigger the buzzer."));

        var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setAlignItems(Alignment.CENTER);

        buttonLayout.add(new BeepButton("Short Beep", 100));
        buttonLayout.add(new BeepButton("Medium Beep", 300));
        buttonLayout.add(new BeepButton("Long Beep", 500));

        add(buttonLayout);

        logs = new LogGrid();
        add(logs);
    }

    private class BeepButton extends Button {
        public BeepButton(String label, int durationMs) {
            super(label, VaadinIcon.BELL.create());
            addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            getStyle()
                    .setMinWidth("120px")
                    .setMarginRight("10px");

            addClickListener(e -> {
                new Thread(() -> joystickService.beep(durationMs)).start();
                logs.addLine("Buzzer triggered: " + label + " (" + durationMs + "ms)");
            });
        }
    }
}
