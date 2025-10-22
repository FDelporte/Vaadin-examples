package be.webtechie.vaadin.pi4j.service.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This example demonstrates the temperature and humidity component on the CrowPi.
 * As Java does not allow for precise enough timings itself, this component does not use Pi4J to retrieve the pulses
 * of the GPIO pin for DHT11 sensor and instead relies on a linux system driver which reads the pulses and write the
 * results into a file.ut.
 * <p>
 * A clean alternative would be using a separate microcontroller which handles the super precise timing-based communication itself and
 * interacts with the Raspberry Pi using I²C, SPI or any other bus. This would offload the work and guarantee even more accurate results. As
 * the CrowPi does not have such a dedicated microcontroller though, using this driver was the best available approach.
 */
public class DHT11Component {
    /**
     * Polling interval of the file reading poller. Do not go to fast it might cause some issues.
     */
    public final static int DEFAULT_POLLING_DELAY_MS = 1000;
    /**
     * Default paths to the files which are written by the DHT11 driver
     */
    private final static String DEFAULT_HUMI_PATH = "/sys/devices/platform/dht11@4/iio:device0/in_humidityrelative_input";
    private final static String DEFAULT_TEMP_PATH = "/sys/devices/platform/dht11@4/iio:device0/in_temp_input";
    private final Logger logger = LoggerFactory.getLogger(DHT11Component.class);
    /**
     * Paths effectively used to read the values
     */
    private final String humiPath;
    private final String tempPath;
    public List<HumiTempMeasurement> humiTempMeasurements = new ArrayList<>();

    /**
     * Creates a new humidity and temperature sensor component with default path and polling interval
     */
    public DHT11Component() {
        this(DEFAULT_HUMI_PATH, DEFAULT_TEMP_PATH, DEFAULT_POLLING_DELAY_MS);
    }

    /**
     * Creates a new humidity and temperature sensor component with default paths and custom polling interval
     *
     * @param pollingDelayMs Delay in millis between reading measurement values
     */
    public DHT11Component(int pollingDelayMs) {
        this(DEFAULT_HUMI_PATH, DEFAULT_TEMP_PATH, pollingDelayMs);
    }

    /**
     * Creates a new humidity and temperature sensor component with custom paths and polling interval
     *
     * @param humiPath       Path to the file containing humidity
     * @param tempPath       Path to the file containing temperature
     * @param pollingDelayMs Polling cycle of reading the measured values
     */
    public DHT11Component(String humiPath, String tempPath, int pollingDelayMs) {
        this.humiPath = humiPath;
        this.tempPath = tempPath;
    }

    /**
     * Poller class which implements {@link Runnable} to be used with {@link ScheduledExecutorService} for repeated execution.
     * This poller consecutively starts reads the values in the humidity and temperature files
     */
    public HumiTempMeasurement getMeasurement() {
        double humidity = 0;
        double temperature = 0;

        // Read humidity file and convert to value
        try {
            humidity = convertToValue(readFile(humiPath));
        } catch (IOException ignored) {
        }
        // Read temperature file and convert to value
        try {
            temperature = convertToValue(readFile(tempPath));
        } catch (IOException ignored) {
        }

        var measurement = new HumiTempMeasurement(temperature, humidity);
        humiTempMeasurements.add(measurement);
        logger.debug("New measurement: {}", measurement);
        return measurement;
    }

    /**
     * Reads a specified file and returns the first line as string
     *
     * @param path Path to the file
     * @return First line of the file as string
     * @throws IOException If the reading fails the IOException is thrown
     */
    private String readFile(String path) throws IOException {
        try (var input = new BufferedReader(new FileReader(path))) {
            return input.readLine();
        }
    }

    /**
     * Calculates and converts a string into a temperature or humidity value
     *
     * @param line Pass the a line of a humidity or temperature file here
     * @return Return the calculated value as double
     */
    private double convertToValue(String line) {
        return Double.parseDouble(line) / 1000;
    }

    public record HumiTempMeasurement(LocalDateTime timestamp, double temperature, double humidity) {
        public HumiTempMeasurement(double temperature, double humidity) {
            this(LocalDateTime.now(), temperature, humidity);
        }

        public static HumiTempMeasurement random() {
            return new HumiTempMeasurement(
                    Math.random() * 65 - 20,  // Temperature: -20 to +45°C
                    Math.random() * 100       // Humidity: 0 to 100%
            );
        }
    }
}
