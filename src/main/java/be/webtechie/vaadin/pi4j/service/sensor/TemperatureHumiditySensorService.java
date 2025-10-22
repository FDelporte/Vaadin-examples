package be.webtechie.vaadin.pi4j.service.sensor;

import be.webtechie.vaadin.pi4j.config.CrowPiConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import com.pi4j.context.Context;
import com.pi4j.drivers.sensor.environment.bmx280.Bmx280Driver;
import com.pi4j.io.i2c.I2C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnExpression("${crowpi.config.hasBMX280Sensor:false}")
public class TemperatureHumiditySensorService {

    private static final Logger logger = LoggerFactory.getLogger(TemperatureHumiditySensorService.class);
    private final Bmx280Driver sensor;
    private final ComponentEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;

    public TemperatureHumiditySensorService(Context pi4j, CrowPiConfig config, ComponentEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        var i2c = pi4j.create(I2C.newConfigBuilder(pi4j)
                .bus(config.getI2cBus())
                .device(config.getI2cDeviceSensor()));

        this.sensor = new Bmx280Driver(i2c);
        logger.info("BMX280 temperature and humidity sensor initialized");

        // Start polling
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::pollSensor, 0, 1, TimeUnit.SECONDS);
    }

    private void pollSensor() {
        try {
            var measurement = sensor.readMeasurement();
            eventPublisher.publish(ChangeListener.ChangeType.SENSOR, measurement);
        } catch (Exception e) {
            logger.error("Error reading sensor: {}", e.getMessage());
        }
    }

    public Bmx280Driver.Measurement getMeasurement() {
        return sensor.readMeasurement();
    }
}