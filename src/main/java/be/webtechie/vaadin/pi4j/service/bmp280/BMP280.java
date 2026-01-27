package be.webtechie.vaadin.pi4j.service.bmp280;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BMP280 Barometric Pressure and Temperature Sensor driver.
 * Communicates via I2C at address 0x76.
 *
 * Based on the Bosch BMP280 datasheet and Python reference implementation.
 */
public class BMP280 {

    private static final Logger logger = LoggerFactory.getLogger(BMP280.class);

    // BMP280 I2C address (SDO = 0)
    public static final int DEFAULT_I2C_ADDRESS = 0x76;

    // BMP280 chip ID
    private static final int BMP280_ID_VALUE = 0x58;

    // Register addresses
    private static final int BMP280_ID_REG = 0xD0;
    private static final int BMP280_RESET_REG = 0xE0;
    private static final int BMP280_STATUS_REG = 0xF3;
    private static final int BMP280_CTRL_MEAS_REG = 0xF4;
    private static final int BMP280_CONFIG_REG = 0xF5;

    // Data registers
    private static final int BMP280_PRESS_MSB_REG = 0xF7;
    private static final int BMP280_PRESS_LSB_REG = 0xF8;
    private static final int BMP280_PRESS_XLSB_REG = 0xF9;
    private static final int BMP280_TEMP_MSB_REG = 0xFA;
    private static final int BMP280_TEMP_LSB_REG = 0xFB;
    private static final int BMP280_TEMP_XLSB_REG = 0xFC;

    // Calibration registers
    private static final int BMP280_DIG_T1_LSB_REG = 0x88;
    private static final int BMP280_DIG_T2_LSB_REG = 0x8A;
    private static final int BMP280_DIG_T3_LSB_REG = 0x8C;
    private static final int BMP280_DIG_P1_LSB_REG = 0x8E;
    private static final int BMP280_DIG_P2_LSB_REG = 0x90;
    private static final int BMP280_DIG_P3_LSB_REG = 0x92;
    private static final int BMP280_DIG_P4_LSB_REG = 0x94;
    private static final int BMP280_DIG_P5_LSB_REG = 0x96;
    private static final int BMP280_DIG_P6_LSB_REG = 0x98;
    private static final int BMP280_DIG_P7_LSB_REG = 0x9A;
    private static final int BMP280_DIG_P8_LSB_REG = 0x9C;
    private static final int BMP280_DIG_P9_LSB_REG = 0x9E;

    private final I2C i2c;

    // Calibration parameters
    private int digT1;
    private int digT2;
    private int digT3;
    private int digP1;
    private int digP2;
    private int digP3;
    private int digP4;
    private int digP5;
    private int digP6;
    private int digP7;
    private int digP8;
    private int digP9;

    // Temperature fine value used in pressure compensation
    private double tFine;

    /**
     * Creates a new BMP280 sensor instance.
     *
     * @param pi4j    Pi4J context
     * @param i2cBus  I2C bus number
     * @param address I2C device address
     */
    public BMP280(Context pi4j, int i2cBus, int address) {
        var i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("BMP280-I2C")
                .name("BMP280 Barometric Sensor")
                .bus(i2cBus)
                .device(address)
                .build();
        this.i2c = pi4j.create(i2cConfig);
    }

    /**
     * Initializes the BMP280 sensor.
     *
     * @return true if initialization was successful, false otherwise
     */
    public boolean begin() {
        // Verify chip ID
        int chipId = readByte(BMP280_ID_REG);
        if (chipId != BMP280_ID_VALUE) {
            logger.error("BMP280 chip ID mismatch: expected 0x{}, got 0x{}",
                    Integer.toHexString(BMP280_ID_VALUE), Integer.toHexString(chipId));
            return false;
        }

        // Load calibration data
        loadCalibration();

        // Configure sensor
        // CTRL_MEAS: Temperature oversampling x16, Pressure oversampling x16, Normal mode
        int ctrlMeas = 0xFF;
        // CONFIG: Standby 0.5ms, Filter coefficient 16
        int config = 0x14;

        writeByte(BMP280_CTRL_MEAS_REG, ctrlMeas);
        writeByte(BMP280_CONFIG_REG, config);

        logger.info("BMP280 initialized successfully");
        return true;
    }

    /**
     * Loads calibration parameters from the sensor.
     */
    private void loadCalibration() {
        // Temperature calibration
        digT1 = readU16(BMP280_DIG_T1_LSB_REG);
        digT2 = readS16(BMP280_DIG_T2_LSB_REG);
        digT3 = readS16(BMP280_DIG_T3_LSB_REG);

        // Pressure calibration
        digP1 = readU16(BMP280_DIG_P1_LSB_REG);
        digP2 = readS16(BMP280_DIG_P2_LSB_REG);
        digP3 = readS16(BMP280_DIG_P3_LSB_REG);
        digP4 = readS16(BMP280_DIG_P4_LSB_REG);
        digP5 = readS16(BMP280_DIG_P5_LSB_REG);
        digP6 = readS16(BMP280_DIG_P6_LSB_REG);
        digP7 = readS16(BMP280_DIG_P7_LSB_REG);
        digP8 = readS16(BMP280_DIG_P8_LSB_REG);
        digP9 = readS16(BMP280_DIG_P9_LSB_REG);

        logger.debug("BMP280 calibration loaded: T1={}, T2={}, T3={}", digT1, digT2, digT3);
    }

    /**
     * Reads temperature and pressure from the sensor.
     *
     * @return Measurement containing temperature (°C) and pressure (Pa)
     */
    public Measurement read() {
        // Read temperature raw data
        int tempXlsb = readByte(BMP280_TEMP_XLSB_REG);
        int tempLsb = readByte(BMP280_TEMP_LSB_REG);
        int tempMsb = readByte(BMP280_TEMP_MSB_REG);
        int adcT = (tempMsb << 12) | (tempLsb << 4) | (tempXlsb >> 4);

        // Calculate temperature (must be done first as it sets tFine for pressure)
        double temperature = compensateTemperature(adcT);

        // Read pressure raw data
        int pressXlsb = readByte(BMP280_PRESS_XLSB_REG);
        int pressLsb = readByte(BMP280_PRESS_LSB_REG);
        int pressMsb = readByte(BMP280_PRESS_MSB_REG);
        int adcP = (pressMsb << 12) | (pressLsb << 4) | (pressXlsb >> 4);

        // Calculate pressure
        double pressure = compensatePressure(adcP);

        return new Measurement(temperature, pressure);
    }

    /**
     * Compensates raw temperature reading using calibration data.
     *
     * @param adcT Raw temperature value
     * @return Temperature in degrees Celsius
     */
    private double compensateTemperature(int adcT) {
        double var1 = (adcT / 16384.0 - digT1 / 1024.0) * digT2;
        double var2 = ((adcT / 131072.0 - digT1 / 8192.0) *
                (adcT / 131072.0 - digT1 / 8192.0)) * digT3;
        tFine = var1 + var2;
        return (var1 + var2) / 5120.0;
    }

    /**
     * Compensates raw pressure reading using calibration data.
     *
     * @param adcP Raw pressure value
     * @return Pressure in Pascal (Pa)
     */
    private double compensatePressure(int adcP) {
        double var1 = (tFine / 2.0) - 64000.0;
        double var2 = var1 * var1 * digP6 / 32768.0;
        var2 = var2 + var1 * digP5 * 2.0;
        var2 = (var2 / 4.0) + (digP4 * 65536.0);
        var1 = (digP3 * var1 * var1 / 524288.0 + digP2 * var1) / 524288.0;
        var1 = (1.0 + var1 / 32768.0) * digP1;

        if (var1 == 0.0) {
            return 0; // Avoid division by zero
        }

        double pressure = 1048576.0 - adcP;
        pressure = (pressure - (var2 / 4096.0)) * 6250.0 / var1;
        var1 = digP9 * pressure * pressure / 2147483648.0;
        var2 = pressure * digP8 / 32768.0;
        pressure = pressure + (var1 + var2 + digP7) / 16.0;

        return pressure;
    }

    private int readByte(int register) {
        return i2c.readRegister(register) & 0xFF;
    }

    private int readU16(int register) {
        int lsb = i2c.readRegister(register) & 0xFF;
        int msb = i2c.readRegister(register + 1) & 0xFF;
        return (msb << 8) | lsb;
    }

    private int readS16(int register) {
        int result = readU16(register);
        if (result > 32767) {
            result -= 65536;
        }
        return result;
    }

    private void writeByte(int register, int value) {
        i2c.writeRegister(register, (byte) value);
    }

    /**
     * Measurement record containing temperature and pressure readings.
     */
    public record Measurement(double temperature, double pressure) {
        /**
         * Returns pressure in hectopascal (hPa), commonly used in meteorology.
         */
        public double pressureHPa() {
            return pressure / 100.0;
        }

        /**
         * Returns pressure in kilopascal (kPa).
         */
        public double pressureKPa() {
            return pressure / 1000.0;
        }

        @Override
        public String toString() {
            return String.format("Temperature: %.2f°C, Pressure: %.2f hPa (%.3f kPa)",
                    temperature, pressureHPa(), pressureKPa());
        }
    }
}
