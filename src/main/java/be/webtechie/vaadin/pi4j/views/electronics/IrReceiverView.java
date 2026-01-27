package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.ir.IrCode;
import be.webtechie.vaadin.pi4j.service.ir.IrService;
import be.webtechie.vaadin.pi4j.service.ir.IrTriggerChanged;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import in.virit.color.NamedColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.ArrayList;
import java.util.List;

/**
 * View for displaying IR (infrared) remote signals.
 * Shows received codes in a grid and allows configuring buzzer trigger.
 */
@PageTitle("IR Receiver")
@Menu(order = 20, icon = LineAwesomeIconUrl.SATELLITE_DISH_SOLID)
public class IrReceiverView extends VerticalLayout implements ChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(IrReceiverView.class);
    private static final int MAX_CODES = 50;

    private final ComponentEventPublisher publisher;
    private final IrService irService;
    private final List<IrCode> receivedCodes = new ArrayList<>();
    private final Grid<IrCode> codeGrid;
    private final TextField buzzerCodeField;
    private final Span lastCodeDisplay;
    private UI ui;

    public IrReceiverView(ComponentEventPublisher publisher, IrService irService) {
        this.publisher = publisher;
        this.irService = irService;

        setMargin(true);
        setSpacing(true);

        add(new H3("IR Receiver (GPIO 18)"));
        add(new Paragraph("Point your IR remote at the receiver and press buttons to see the codes."));

        // Last received code display
        lastCodeDisplay = new Span("--");
        lastCodeDisplay.getStyle()
                .setFontSize("24px")
                .setFontWeight("bold")
                .setPadding("10px 20px")
                .setBackground(NamedColor.LIGHTGRAY.toString())
                .setBorderRadius("8px");

        var lastCodeLayout = new HorizontalLayout();
        lastCodeLayout.setAlignItems(Alignment.CENTER);
        lastCodeLayout.add(new Span("Last code: "), lastCodeDisplay);
        add(lastCodeLayout);

        // Buzzer trigger configuration
        add(new H3("Buzzer Trigger"));
        add(new Paragraph("Enter an IR code (hex) to trigger the buzzer when received:"));

        buzzerCodeField = new TextField("IR Code (hex)");
        buzzerCodeField.setPlaceholder("e.g., 0x45 or 45");
        buzzerCodeField.setWidth("150px");
        buzzerCodeField.setClearButtonVisible(true);

        int currentTrigger = irService.getBuzzerTriggerCode();
        if (currentTrigger >= 0) {
            buzzerCodeField.setValue(String.format("0x%02X", currentTrigger));
        }

        // Update trigger immediately when value changes
        buzzerCodeField.addValueChangeListener(e -> {
            String value = e.getValue().trim();
            if (value.isEmpty()) {
                irService.setBuzzerTriggerCode(-1);
                buzzerCodeField.setInvalid(false);
            } else {
                try {
                    int code = parseHexOrDecimal(value);
                    if (code >= 0 && code <= 255) {
                        irService.setBuzzerTriggerCode(code);
                        buzzerCodeField.setInvalid(false);
                    } else {
                        buzzerCodeField.setInvalid(true);
                        buzzerCodeField.setErrorMessage("Code must be 0x00-0xFF");
                    }
                } catch (NumberFormatException ex) {
                    buzzerCodeField.setInvalid(true);
                    buzzerCodeField.setErrorMessage("Invalid hex value");
                }
            }
        });

        var useLastCodeButton = new Button("Use Last Code", e -> {
            if (!receivedCodes.isEmpty()) {
                IrCode lastCode = receivedCodes.get(0);
                buzzerCodeField.setValue(lastCode.getCodeHex());
            }
        });

        var triggerLayout = new HorizontalLayout(buzzerCodeField, useLastCodeButton);
        triggerLayout.setAlignItems(Alignment.BASELINE);
        add(triggerLayout);

        // Code history grid
        add(new H3("Received Codes"));

        codeGrid = new Grid<>(IrCode.class, false);
        codeGrid.addColumn(IrCode::getTimestampFormatted).setHeader("Time").setWidth("150px");
        codeGrid.addColumn(IrCode::getCodeHex).setHeader("Code (Hex)").setWidth("120px");
        codeGrid.addColumn(IrCode::code).setHeader("Code (Dec)").setWidth("120px");
        codeGrid.setItems(receivedCodes);
        codeGrid.setHeight("300px");
        codeGrid.setWidthFull();

        add(codeGrid);

        // Clear button
        var clearButton = new Button("Clear History", VaadinIcon.TRASH.create(), e -> {
            receivedCodes.clear();
            codeGrid.getDataProvider().refreshAll();
        });
        add(clearButton);

        // Test button (for development)
        var testButton = new Button("Simulate Code (0x45)", e -> {
            irService.simulateIrCode(0x45);
        });
        testButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        add(testButton);
    }

    private int parseHexOrDecimal(String value) {
        value = value.trim().toLowerCase();
        if (value.startsWith("0x")) {
            return Integer.parseInt(value.substring(2), 16);
        } else if (value.matches("[0-9a-f]+") && value.length() <= 2) {
            // Assume hex if it looks like hex
            return Integer.parseInt(value, 16);
        } else {
            return Integer.parseInt(value);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        this.ui = attachEvent.getUI();
        publisher.addListener(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        publisher.removeListener(this);
    }

    @Override
    public <T> void onMessage(ChangeType type, T message) {
        if (!type.equals(ChangeType.IR)) {
            return;
        }

        // Handle trigger change (e.g., from KEY press)
        if (message instanceof IrTriggerChanged triggerChanged) {
            ui.access(() -> {
                String newValue = triggerChanged.getTriggerCodeHex();
                // Only update if different to avoid triggering value change listener
                if (!buzzerCodeField.getValue().equals(newValue)) {
                    buzzerCodeField.setValue(newValue);
                }
            });
            return;
        }

        // Handle received IR code
        if (!(message instanceof IrCode irCode)) {
            return;
        }

        logger.debug("IR code received in view: {}", irCode);

        ui.access(() -> {
            // Add to beginning of list
            receivedCodes.add(0, irCode);

            // Limit list size
            while (receivedCodes.size() > MAX_CODES) {
                receivedCodes.remove(receivedCodes.size() - 1);
            }

            // Update displays
            lastCodeDisplay.setText(irCode.getCodeHex());
            lastCodeDisplay.getStyle().setBackground(NamedColor.LIMEGREEN.toString());

            codeGrid.getDataProvider().refreshAll();
        });

        // Reset color after delay (in background thread)
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            ui.access(() -> lastCodeDisplay.getStyle().setBackground(NamedColor.LIGHTGRAY.toString()));
        }).start();
    }
}
