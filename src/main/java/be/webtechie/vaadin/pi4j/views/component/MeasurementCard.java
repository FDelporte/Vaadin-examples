package be.webtechie.vaadin.pi4j.views.component;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import in.virit.color.Color;

/**
 * A card component for displaying a measurement value with label and unit.
 * Useful for sensor readings like temperature, pressure, humidity, etc.
 */
public class MeasurementCard extends Card {

    private final Span valueSpan;

    public MeasurementCard(String label, String unit, VaadinIcon icon, Color color) {
        addThemeVariants(CardVariant.LUMO_ELEVATED);

        // Icon with colored background
        Icon mediaIcon = icon.create();
        mediaIcon.setSize("48px");
        mediaIcon.getStyle()
                .setColor("white")
                .setBackground(color.toString())
                .setPadding("12px")
                .setBorderRadius("50%");
        setMedia(mediaIcon);

        setTitle(label);

        // Value with unit inline
        valueSpan = new Span("--");
        valueSpan.getStyle()
                .setFontSize("36px")
                .setFontWeight("bold")
                .setColor(color.toString());

        var unitSpan = new Span(unit);
        unitSpan.getStyle()
                .setFontSize("18px")
                .setColor("var(--lumo-secondary-text-color)")
                .setMarginLeft("4px")
                .setAlignSelf(com.vaadin.flow.dom.Style.AlignSelf.FLEX_END)
                .setPaddingBottom("4px");

        var valueLayout = new HorizontalLayout(valueSpan, unitSpan);
        valueLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        valueLayout.setSpacing(false);

        add(valueLayout);
    }

    public void setValue(String value) {
        valueSpan.setText(value);
    }
}
