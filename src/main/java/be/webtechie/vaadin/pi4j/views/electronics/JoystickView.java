package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.event.JoystickEvent;
import be.webtechie.vaadin.pi4j.event.KeyStateEvent;
import be.webtechie.vaadin.pi4j.service.joystick.JoystickDirection;
import be.webtechie.vaadin.pi4j.service.joystick.JoystickService;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import be.webtechie.vaadin.pi4j.views.component.SnakeGame;
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
import in.virit.color.NamedColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * View for displaying joystick input from the PCF8574-based joystick.
 * This view is dynamically registered by JoystickService when the board has joystick support.
 */
@PageTitle("Joystick")
@Menu(order = 18, icon = LineAwesomeIconUrl.GAMEPAD_SOLID)
public class JoystickView extends HardwareDemoView {

    private static final Logger logger = LoggerFactory.getLogger(JoystickView.class);

    private final JoystickService joystickService;
    private final LogGrid logs;

    private final DirectionIndicator upIndicator;
    private final DirectionIndicator downIndicator;
    private final DirectionIndicator leftIndicator;
    private final DirectionIndicator rightIndicator;
    private final BuzzerButton centerButton;
    private final SnakeGame snakeGame;

    public JoystickView(ComponentEventBus eventBus, JoystickService joystickService, SnakeGame snakeGame) {
        this.joystickService = joystickService;
        this.snakeGame = snakeGame;

        eventBus.subscribe(this, KeyStateEvent.class, this::onKeyEvent);
        eventBus.subscribe(this, JoystickEvent.class, this::onJoystickEvent);

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

        var controlsRow = new HorizontalLayout(joystickLayout, snakeGame);
        controlsRow.setAlignItems(Alignment.CENTER);
        controlsRow.setSpacing(true);
        controlsRow.getStyle().setGap("2rem");

        add(controlsRow);

        logs = new LogGrid();
        add(logs);
    }

    private void onKeyEvent(KeyStateEvent event) {
        var isPressed = event.isPressed();
        logger.debug("Key event: {} - Is pressed: {}", event.getState(), isPressed);

        if (isPressed) {
            logger.info("Key pressed - triggering buzzer");
            new Thread(() -> joystickService.beep(150)).start();
            centerButton.getStyle().setBackground(NamedColor.LIMEGREEN.toString());
            logs.addLine("Buzzer triggered (key press)!");
        } else {
            centerButton.getStyle().setBackground(NamedColor.ORANGE.toString());
        }
    }

    private void onJoystickEvent(JoystickEvent event) {
        var direction = event.getDirection();
        logger.debug("Joystick direction received: {}", direction);

        // Reset all indicators
        upIndicator.setActive(false);
        downIndicator.setActive(false);
        leftIndicator.setActive(false);
        rightIndicator.setActive(false);

        // Activate the appropriate indicator and control snake
        switch (direction) {
            case UP -> {
                upIndicator.setActive(true);
                snakeGame.setDirection(SnakeGame.Direction.UP);
            }
            case DOWN -> {
                downIndicator.setActive(true);
                snakeGame.setDirection(SnakeGame.Direction.DOWN);
            }
            case LEFT -> {
                leftIndicator.setActive(true);
                snakeGame.setDirection(SnakeGame.Direction.LEFT);
            }
            case RIGHT -> {
                rightIndicator.setActive(true);
                snakeGame.setDirection(SnakeGame.Direction.RIGHT);
            }
            case NONE -> { /* nothing to highlight */ }
        }

        if (direction != JoystickDirection.NONE) {
            logs.addLine("Direction: " + direction);
        }
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
                new Thread(() -> joystickService.beep(150)).start();
                logs.addLine("Buzzer triggered!");
            });
        }
    }
}
