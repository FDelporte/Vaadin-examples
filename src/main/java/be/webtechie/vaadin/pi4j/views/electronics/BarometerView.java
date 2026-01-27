package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.bmp280.BMP280;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import be.webtechie.vaadin.pi4j.views.component.MeasurementCard;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import in.virit.color.NamedColor;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * View for displaying BMP280 barometric pressure and temperature readings.
 * This view is dynamically registered by BMP280Service when the board has BMP280 support.
 */
@PageTitle("Barometer")
// @Route("barometerview") - Conditionally registered by BMP280Service
@Menu(order = 17, icon = LineAwesomeIconUrl.CLOUD_SUN_SOLID)
public class BarometerView extends VerticalLayout implements ChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(BarometerView.class);

    private final ComponentEventPublisher publisher;
    private final UI ui;
    private final LogGrid logs;

    private final MeasurementCard temperatureCard;
    private final MeasurementCard pressureCard;
    private final MeasurementCard pressureHpaCard;

    public BarometerView(ComponentEventPublisher publisher) {
        this.publisher = publisher;
        this.ui = UI.getCurrent();

        setMargin(true);
        setSpacing(true);

        add(new H3("BMP280 Barometric Sensor"));

        temperatureCard = new MeasurementCard("Temperature", "°C",
                VaadinIcon.FIRE, NamedColor.TOMATO);
        pressureCard = new MeasurementCard("Pressure", "Pa",
                VaadinIcon.COMPRESS, NamedColor.STEELBLUE);
        pressureHpaCard = new MeasurementCard("Pressure", "hPa",
                VaadinIcon.DASHBOARD, NamedColor.MEDIUMPURPLE);

        var cardsLayout = new HorizontalLayout(temperatureCard, pressureCard, pressureHpaCard);
        cardsLayout.setSpacing(true);
        add(cardsLayout);

        logs = new LogGrid();
        add(logs);
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        publisher.addListener(this);
    }

    @Override
    public void onDetach(DetachEvent detachEvent) {
        publisher.removeListener(this);
    }

    @Override
    public <T> void onMessage(ChangeListener.ChangeType type, T message) {
        if (!type.equals(ChangeType.BMP280) || !(message instanceof BMP280.Measurement)) {
            return;
        }

        var measurement = (BMP280.Measurement) message;
        logger.debug("BMP280 measurement received: {}", measurement);

        ui.access(() -> {
            temperatureCard.setValue(String.format("%.1f", measurement.temperature()));
            pressureCard.setValue(String.format("%.0f", measurement.pressure()));
            pressureHpaCard.setValue(String.format("%.1f", measurement.pressureHPa()));

            logs.addLine(measurement.toString());
        });
    }
}
