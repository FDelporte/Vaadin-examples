package be.webtechie.vaadin.pi4j.service.sensor;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.DhtMeasurementEvent;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.TempHumidityView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DHT11 sensor service using one-wire protocol.
 * Used for CrowPi 1 and CrowPi 2 which don't have I2C-based humidity sensor.
 */
@Service
public class DHT11OneWireService {

    private static final Logger logger = LoggerFactory.getLogger(DHT11OneWireService.class);
    private final DHT11OneWireComponent component;
    private final ApplicationEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;

    public DHT11OneWireService(BoardConfig config, ApplicationEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;

        // Only initialize for boards that have DHT11 but NOT via I2C (CrowPi 1, 2)
        // CrowPi 3 uses I2C-based sensor (handled by DHT11I2CService)
        if (!config.hasDht11() || config.getI2cDeviceHumidityTemperatureSensor() != 0x00) {
            logger.info("DHT11 OneWire sensor not available on this board");
            this.component = null;
            this.scheduler = null;
            return;
        }

        logger.info("DHT11OneWireService constructor called - service is starting!");

        this.component = new DHT11OneWireComponent();
        logger.info("DHT11 humidity and temperature sensor initialized");

        // Register the view for this feature
        pi4JService.registerView(TempHumidityView.class);

        // Start polling
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::pollSensor, 0, 1, TimeUnit.SECONDS);
    }

    public boolean isAvailable() {
        return component != null;
    }

    private void pollSensor() {
        try {
            var measurement = component.getMeasurement();
            eventPublisher.publishEvent(new DhtMeasurementEvent(this, measurement.temperature(), measurement.humidity()));
        } catch (Exception e) {
            logger.error("Error reading DHT11 sensor: {}", e.getMessage());
        }
    }
}
