package be.webtechie.vaadin.pi4j.service.sensor;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.electronics.TempHumidityView;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CImplementation;
import com.pi4j.plugin.ffm.common.HexFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DHT11 sensor service using I2C protocol.
 * Used for CrowPi 3 which has I2C-based humidity sensor.
 */
@Service
public class DHT11I2CService {

    private static final Logger logger = LoggerFactory.getLogger(DHT11I2CService.class);

    private final ComponentEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;
    private final I2C i2cSensor;

    public DHT11I2CService(Context pi4j, BoardConfig config, ComponentEventPublisher eventPublisher, Pi4JService pi4JService) {
        this.eventPublisher = eventPublisher;

        // Only initialize for boards that have DHT11 via I2C (CrowPi 3)
        if (!config.hasDht11() || config.getI2cDeviceHumidityTemperatureSensor() == 0x00) {
            logger.info("DHT11 I2C sensor not available on this board");
            this.i2cSensor = null;
            this.scheduler = null;
            return;
        }

        logger.info("DHT11I2CService constructor called - service is starting!");

        var i2cBus = config.getI2cBus();
        var i2cAddress = config.getI2cDeviceHumidityTemperatureSensor();

        var i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("I2C-DHT11-" + i2cBus + "-" + HexFormatter.format(i2cAddress))
                .bus(i2cBus)
                .device((int) i2cAddress)
                .i2cImplementation(I2CImplementation.DIRECT)
                .build();
        i2cSensor = pi4j.create(i2cConfig);

        logger.info("DHT11 humidity and temperature sensor on I2C initialized");

        // Register the view for this feature
        pi4JService.registerView(TempHumidityView.class);

        // Start polling
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::pollSensor, 0, 1, TimeUnit.SECONDS);
    }

    public boolean isAvailable() {
        return i2cSensor != null;
    }

    private void pollSensor() {
        try {
            // Check initialization status
            byte[] statusData = new byte[1];
            i2cSensor.readRegister(0x71, statusData, 0, 1);
            if ((statusData[0] | 0x08) == 0) {
                logger.error("I2C DHT11 initialization error");
                return;
            }

            // Trigger measurement
            i2cSensor.writeRegister(0xAC, new byte[]{0x33, 0x00});
            Thread.sleep(100);

            // Read measurement data
            byte[] data = new byte[7];
            i2cSensor.readRegister(0x71, data, 0, 7);

            // Parse temperature
            int tRaw = ((data[3] & 0x0F) << 16) | ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
            double temperature = (200.0 * tRaw / Math.pow(2, 20)) - 50.0;

            // Parse humidity
            int hRaw = ((data[3] & 0xF0) >> 4) | ((data[1] & 0xFF) << 12) | ((data[2] & 0xFF) << 4);
            double humidity = 100.0 * hRaw / Math.pow(2, 20);

            logger.trace("Temperature: {}°C, Humidity: {}%", temperature, humidity);

            eventPublisher.publish(ChangeListener.ChangeType.DHT11, new HumidityTemperatureMeasurement(temperature, humidity));
        } catch (Exception e) {
            logger.error("Error reading DHT11 sensor: {}", e.getMessage());
        }
    }
}
