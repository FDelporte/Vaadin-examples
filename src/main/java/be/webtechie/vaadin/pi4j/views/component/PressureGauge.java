package be.webtechie.vaadin.pi4j.views.component;

import in.virit.Gauge;
import in.virit.color.NamedColor;

/**
 * A specialized gauge for displaying barometric pressure values with appropriate
 * color coding and range settings for atmospheric pressure measurements.
 * Typical sea-level pressure ranges from ~950 hPa (low) to ~1050 hPa (high).
 */
public class PressureGauge extends Gauge {

    public PressureGauge() {
        this(1013.25); // Standard atmospheric pressure
    }

    public PressureGauge(double pressure) {
        super();
        setupPressureDefaults();
        setPressure(pressure);
    }

    private void setupPressureDefaults() {
        setMinValue(950);
        setMaxValue(1050);
        setState("gaugeType", "pressure");
        setArc(new GaugeArc()
            .setSubArcs(
                new GaugeSubArc(980, NamedColor.PURPLE).setTooltip("Very Low - Storm"),
                new GaugeSubArc(1000, NamedColor.BLUE).setTooltip("Low - Rain likely"),
                new GaugeSubArc(1020, NamedColor.GREEN).setTooltip("Normal"),
                new GaugeSubArc(1035, NamedColor.ORANGE).setTooltip("High - Fair weather"),
                new GaugeSubArc(1050, NamedColor.RED).setTooltip("Very High")
            )
        );
    }

    public void setPressure(double pressure) {
        setValue(pressure);
    }

    public void setPressureRange(double minPressure, double maxPressure) {
        setMinValue(minPressure);
        setMaxValue(maxPressure);
    }
}
