package be.webtechie.vaadin.pi4j.service.sensor;

import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnExpression("${crowpi.config.hasDHT11Sensor:false}")
public class DHT11Service {

    private static final Logger logger = LoggerFactory.getLogger(DHT11Service.class);
    private final DHT11Component component;
    private final ComponentEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;

    public DHT11Service(ComponentEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.component = new DHT11Component();
        logger.info("DHT11 humidity and temperature sensor initialized");

        // Start polling
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::pollSensor, 0, 1, TimeUnit.SECONDS);
    }

    private void pollSensor() {
        try {
            var measurement = component.getMeasurement();
            eventPublisher.publish(ChangeListener.ChangeType.DHT11, measurement);
        } catch (Exception e) {
            logger.error("Error reading DHT11 sensor: {}", e.getMessage());
        }
    }

    public Object getMeasurement() {
        return component.getMeasurement();
    }
}