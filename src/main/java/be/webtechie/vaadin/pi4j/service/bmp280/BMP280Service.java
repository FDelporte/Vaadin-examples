package be.webtechie.vaadin.pi4j.service.bmp280;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.BarometerView;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for reading BMP280 barometric pressure and temperature sensor.
 * Publishes readings via ComponentEventPublisher for UI updates.
 */
@Service
public class BMP280Service {

    private static final Logger logger = LoggerFactory.getLogger(BMP280Service.class);
    private static final long POLLING_INTERVAL_MS = 2000; // Poll every 2 seconds

    private final BMP280 sensor;
    private final ComponentEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;
    private final boolean mockMode;
    private final Random random = new Random();

    public BMP280Service(Context pi4j, BoardConfig config, ComponentEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;

        if (!config.hasBmp280() || config.getI2cDeviceBmp280() == 0x00) {
            logger.info("BMP280 sensor not available on this board");
            this.sensor = null;
            this.scheduler = null;
            this.mockMode = false;
            return;
        }

        logger.info("Initializing BMP280 sensor on I2C bus {} at address 0x{}",
                config.getI2cBus(), Integer.toHexString(config.getI2cDeviceBmp280() & 0xFF));

        BMP280 tempSensor = null;
        boolean useMockMode = false;

        try {
            tempSensor = new BMP280(pi4j, config.getI2cBus(), config.getI2cDeviceBmp280() & 0xFF);

            if (!tempSensor.begin()) {
                logger.warn("Failed to initialize BMP280 sensor, using mock data");
                tempSensor = null;
                useMockMode = true;
            } else {
                logger.info("BMP280 sensor initialized and polling started");
            }
        } catch (Exception e) {
            logger.warn("BMP280 sensor not available (running with mock hardware): {}", e.getMessage());
            tempSensor = null;
            useMockMode = true;
        }

        this.sensor = tempSensor;
        this.mockMode = useMockMode;

        // Always register the view if board config says it has BMP280
        pi4JService.registerView(BarometerView.class);

        // Start polling (real or mock)
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::pollSensor, 0, POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns true if the BMP280 sensor is available and initialized.
     */
    public boolean isAvailable() {
        return sensor != null && scheduler != null;
    }

    /**
     * Reads the current sensor values.
     *
     * @return Measurement containing temperature and pressure, or null if unavailable
     */
    public BMP280.Measurement read() {
        if (sensor == null) {
            return null;
        }
        return sensor.read();
    }

    private void pollSensor() {
        try {
            BMP280.Measurement measurement;
            if (mockMode) {
                // Generate mock data with slight variations
                double temperature = 20.0 + random.nextDouble() * 5.0; // 20-25°C
                double pressure = 101300 + random.nextDouble() * 500;  // ~1013 hPa
                measurement = new BMP280.Measurement(temperature, pressure);
                logger.trace("BMP280 (mock): {}", measurement);
            } else {
                measurement = sensor.read();
                logger.trace("BMP280: {}", measurement);
            }
            eventPublisher.publish(ChangeListener.ChangeType.BMP280, measurement);
        } catch (Exception e) {
            logger.error("Error reading BMP280 sensor: {}", e.getMessage());
        }
    }
}
