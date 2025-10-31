package be.webtechie.vaadin.pi4j.service.sensor;

import be.webtechie.vaadin.pi4j.config.CrowPi1Config;
import be.webtechie.vaadin.pi4j.config.CrowPi2Config;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnBean({CrowPi1Config.class, CrowPi2Config.class})
public class DHT11OneWireService {

    private static final Logger logger = LoggerFactory.getLogger(DHT11OneWireService.class);
    private final DHT11OneWireComponent component;
    private final ComponentEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;

    public DHT11OneWireService(ComponentEventPublisher eventPublisher) {
        logger.info("DHT11OneWireService constructor called - service is starting!");

        this.eventPublisher = eventPublisher;
        this.component = new DHT11OneWireComponent();
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
}