package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.sensor.HumidityTemperatureMeasurement;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.pi4j.drivers.sensor.environment.bmx280.Bmx280Driver;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import in.virit.EnvironmentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Temperature and Humidity")
// @Route("temperature-humidity") - Conditionally registered by DHT11 services
@Menu(order = 16, icon = LineAwesomeIconUrl.THERMOMETER_EMPTY_SOLID)
public class TempHumidityView extends VerticalLayout implements ChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(TempHumidityView.class);
    private final ComponentEventPublisher publisher;
    private final LogGrid logs;
    private final UI ui;
    private final EnvironmentMonitor environmentMonitor;

    public TempHumidityView(ComponentEventPublisher publisher) {
        this.publisher = publisher;
        this.ui = UI.getCurrent();

        setMargin(true);

        environmentMonitor = new EnvironmentMonitor();
        add(environmentMonitor);

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
    public <T> void onMessage(ChangeType type, T message) {
        if (type.equals(ChangeType.SENSOR)) {
            var measurement = (Bmx280Driver.Measurement) message;
            logger.debug("Message received: {}", measurement);
            logs.addLine("Temperature: " + measurement.getTemperature() + ", humidity: " + measurement.getHumidity());
            ui.access(() -> environmentMonitor.setEnvironmentValues((measurement.getTemperature()), measurement.getHumidity()));
        }
        if (type.equals(ChangeType.DHT11)) {
            var measurement = (HumidityTemperatureMeasurement) message;
            logger.debug("Message received: {}", measurement);
            logs.addLine("Temperature: " + measurement.temperature() + ", humidity: " + measurement.humidity());
            ui.access(() -> environmentMonitor.setEnvironmentValues((measurement.temperature()), measurement.humidity()));
        }
    }
}
