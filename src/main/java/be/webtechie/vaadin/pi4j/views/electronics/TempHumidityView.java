package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.DhtMeasurementEvent;
import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import in.virit.EnvironmentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Temperature and Humidity")
@Menu(order = 16, icon = LineAwesomeIconUrl.THERMOMETER_EMPTY_SOLID)
public class TempHumidityView extends HardwareDemoView {

    private static final Logger logger = LoggerFactory.getLogger(TempHumidityView.class);
    private final LogGrid logs;
    private final EnvironmentMonitor environmentMonitor;

    public TempHumidityView(ComponentEventBus eventBus) {
        eventBus.subscribe(this, DhtMeasurementEvent.class, this::onDhtMeasurement);

        environmentMonitor = new EnvironmentMonitor();
        add(environmentMonitor);

        logs = new LogGrid();
        add(logs);
    }

    private void onDhtMeasurement(DhtMeasurementEvent event) {
        double temperature = event.getTemperature();
        double humidity = event.getHumidity();
        logger.debug("DHT11 measurement received: temp={}, humidity={}", temperature, humidity);

        logs.addLine("Temperature: " + temperature + ", humidity: " + humidity);
        environmentMonitor.setEnvironmentValues(temperature, humidity);
    }
}
