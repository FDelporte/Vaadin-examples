package be.webtechie.vaadin.pi4j.service.sensor;

import be.webtechie.vaadin.pi4j.config.CrowPi3Config;
import be.webtechie.vaadin.pi4j.event.ComponentEventPublisher;
import be.webtechie.vaadin.pi4j.service.ChangeListener;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CImplementation;
import com.pi4j.plugin.ffm.common.HexFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnBean(CrowPi3Config.class)
public class DHT11I2CService {

    private static final Logger logger = LoggerFactory.getLogger(DHT11I2CService.class);

    private static final byte I2C_BUS = 0x01;
    private static final byte I2C_ADDRESS = 0x38;

    private final ComponentEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;
    private final I2C i2cSensor;

    public DHT11I2CService(Context pi4j, ComponentEventPublisher eventPublisher) {
        logger.info("DHT11I2CService constructor called - service is starting!");

        this.eventPublisher = eventPublisher;

        var i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("I2C-DHT11-" + I2C_BUS + "-" + HexFormatter.format(I2C_ADDRESS))
                .bus((int) I2C_BUS)
                .device((int) I2C_ADDRESS)
                .i2cImplementation(I2CImplementation.DIRECT)
                .build();
        i2cSensor = pi4j.create(i2cConfig);

        logger.info("DHT11 humidity and temperature sensor on I2C initialized");

        // Start polling
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::pollSensor, 0, 1, TimeUnit.SECONDS);
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