package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.BMP280Event;
import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import be.webtechie.vaadin.pi4j.views.component.PressureGauge;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import in.virit.TemperatureGauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * View for displaying BMP280 temperature and barometric pressure readings.
 * This view is dynamically registered by BMP280Service when the board has BMP280 support.
 */
@PageTitle("Weather")
// @Route("weatherview") - Conditionally registered by BMP280Service
@Menu(order = 17, icon = LineAwesomeIconUrl.CLOUD_SUN_SOLID)
public class WeatherView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(WeatherView.class);

    private final LogGrid logs;
    private final TemperatureGauge temperatureGauge;
    private final PressureGauge pressureGauge;

    public WeatherView(ComponentEventBus eventBus) {
        eventBus.subscribe(this, BMP280Event.class, this::onBMP280Measurement);

        setMargin(true);
        setSpacing(true);

        // Create gauges layout
        var gaugesLayout = new HorizontalLayout();
        gaugesLayout.setWidthFull();
        gaugesLayout.setJustifyContentMode(JustifyContentMode.AROUND);

        // Temperature section
        var tempSection = new VerticalLayout();
        tempSection.setAlignItems(Alignment.CENTER);
        tempSection.setSpacing(false);

        var tempTitle = new H3("Temperature");
        tempTitle.getStyle().setMargin("0 0 1rem 0");

        temperatureGauge = new TemperatureGauge();
        tempSection.add(tempTitle, temperatureGauge);

        // Pressure section
        var pressureSection = new VerticalLayout();
        pressureSection.setAlignItems(Alignment.CENTER);
        pressureSection.setSpacing(false);

        var pressureTitle = new H3("Pressure");
        pressureTitle.getStyle().setMargin("0 0 1rem 0");

        pressureGauge = new PressureGauge();
        pressureSection.add(pressureTitle, pressureGauge);

        gaugesLayout.add(tempSection, pressureSection);
        add(gaugesLayout);

        logs = new LogGrid();
        add(logs);
    }

    private void onBMP280Measurement(BMP280Event event) {
        var measurement = event.getMeasurement();
        logger.debug("BMP280 measurement received: {}", measurement);

        temperatureGauge.setTemperature(measurement.temperature());
        pressureGauge.setPressure(measurement.pressureHPa());

        logs.addLine(String.format("Temp: %.1f°C, Pressure: %.1f hPa",
                measurement.temperature(), measurement.pressureHPa()));
    }
}
