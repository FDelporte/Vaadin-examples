package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.joystick.JoystickDirection;
import be.webtechie.vaadin.pi4j.service.joystick.JoystickService;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.pi4j.io.gpio.digital.DigitalState;
import in.virit.color.NamedColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * View for displaying joystick input from the PCF8574-based joystick.
 * This view is dynamically registered by JoystickService when the board has joystick support.
 */
@PageTitle("Joystick")
// @Route("joystickview") - Conditionally registered by JoystickService
@Menu(order = 18, icon = LineAwesomeIconUrl.GAMEPAD_SOLID)
public class JoystickView extends VerticalLayout implements ChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(JoystickView.class);

    private final ComponentEventPublisher publisher;
    private final JoystickService joystickService;
    private final LogGrid logs;
    private UI ui;

    private final DirectionIndicator upIndicator;
    private final DirectionIndicator downIndicator;
    private final DirectionIndicator leftIndicator;
    private final DirectionIndicator rightIndicator;
    private final BuzzerButton centerButton;

    public JoystickView(ComponentEventPublisher publisher, JoystickService joystickService) {
        this.publisher = publisher;
        this.joystickService = joystickService;

        setMargin(true);
        setSpacing(true);

        add(new H3("Joystick (PCF8574)"));

        // Create direction indicators in a cross pattern
        upIndicator = new DirectionIndicator(VaadinIcon.ARROW_UP);
        downIndicator = new DirectionIndicator(VaadinIcon.ARROW_DOWN);
        leftIndicator = new DirectionIndicator(VaadinIcon.ARROW_LEFT);
        rightIndicator = new DirectionIndicator(VaadinIcon.ARROW_RIGHT);

        // Center button triggers the buzzer
        centerButton = new BuzzerButton();

        // Layout: top row with UP, middle row with LEFT-CENTER-RIGHT, bottom row with DOWN
        var topRow = new HorizontalLayout(upIndicator);
        topRow.setJustifyContentMode(JustifyContentMode.CENTER);
        topRow.setWidthFull();

        var middleRow = new HorizontalLayout(leftIndicator, centerButton, rightIndicator);
        middleRow.setJustifyContentMode(JustifyContentMode.CENTER);
        middleRow.setAlignItems(Alignment.CENTER);
        middleRow.setSpacing(true);

        var bottomRow = new HorizontalLayout(downIndicator);
        bottomRow.setJustifyContentMode(JustifyContentMode.CENTER);
        bottomRow.setWidthFull();

        var joystickLayout = new VerticalLayout(topRow, middleRow, bottomRow);
        joystickLayout.setSpacing(false);
        joystickLayout.setPadding(false);
        joystickLayout.setAlignItems(Alignment.CENTER);
        joystickLayout.setWidth("250px");

        add(joystickLayout);

        logs = new LogGrid();
        add(logs);
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        this.ui = attachEvent.getUI();
        publisher.addListener(this);
    }

    @Override
    public void onDetach(DetachEvent detachEvent) {
        publisher.removeListener(this);
    }

    @Override
    public <T> void onMessage(ChangeListener.ChangeType type, T message) {
        // Handle KEY press (physical center button)
        if (type.equals(ChangeType.KEY) && message instanceof DigitalState state) {
            if (state == DigitalState.LOW) {  // Button pressed (active low)
                logger.info("Key pressed - triggering buzzer");
                new Thread(() -> joystickService.beep(150)).start();
                ui.access(() -> {
                    centerButton.getStyle().setBackground(NamedColor.LIMEGREEN.toString());
                    logs.addLine("Buzzer triggered (key press)!");
                });
            } else {
                ui.access(() -> {
                    centerButton.getStyle().setBackground(NamedColor.ORANGE.toString());
                });
            }
            return;
        }

        // Handle joystick direction
        if (!type.equals(ChangeType.JOYSTICK) || !(message instanceof JoystickDirection)) {
            return;
        }

        var direction = (JoystickDirection) message;
        logger.debug("Joystick direction received: {}", direction);

        ui.access(() -> {
            // Reset all indicators
            upIndicator.setActive(false);
            downIndicator.setActive(false);
            leftIndicator.setActive(false);
            rightIndicator.setActive(false);

            // Activate the appropriate indicator
            switch (direction) {
                case UP -> upIndicator.setActive(true);
                case DOWN -> downIndicator.setActive(true);
                case LEFT -> leftIndicator.setActive(true);
                case RIGHT -> rightIndicator.setActive(true);
                case NONE -> { /* nothing to highlight */ }
            }

            if (direction != JoystickDirection.NONE) {
                logs.addLine("Direction: " + direction);
            }
        });
    }

    /**
     * A visual indicator for a joystick direction.
     */
    private static class DirectionIndicator extends Div {
        private final NamedColor activeColor = NamedColor.LIMEGREEN;
        private final NamedColor inactiveColor = NamedColor.LIGHTGRAY;

        public DirectionIndicator(VaadinIcon iconType) {
            Icon icon = iconType.create();
            icon.setSize("32px");
            icon.getStyle().setColor("white");

            getStyle()
                    .setWidth("60px")
                    .setHeight("60px")
                    .setBorderRadius("8px")
                    .setBackground(inactiveColor.toString())
                    .setDisplay(com.vaadin.flow.dom.Style.Display.FLEX)
                    .setAlignItems(com.vaadin.flow.dom.Style.AlignItems.CENTER)
                    .setJustifyContent(com.vaadin.flow.dom.Style.JustifyContent.CENTER);

            add(icon);
        }

        public void setActive(boolean active) {
            getStyle().setBackground(active ? activeColor.toString() : inactiveColor.toString());
        }
    }

    /**
     * Center button that triggers the buzzer when clicked.
     */
    private class BuzzerButton extends Button {
        public BuzzerButton() {
            super(VaadinIcon.BELL.create());
            addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            getStyle()
                    .setWidth("60px")
                    .setHeight("60px")
                    .setBorderRadius("50%")
                    .setBackground(NamedColor.ORANGE.toString())
                    .setBorder("3px solid " + NamedColor.DARKORANGE.toString());

            addClickListener(e -> {
                logger.info("Buzzer button clicked");

                // Trigger buzzer in background thread
                new Thread(() -> joystickService.beep(150)).start();

                // Log the action
                logs.addLine("Buzzer triggered!");
            });
        }
    }
}
