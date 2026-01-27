package be.webtechie.vaadin.pi4j.event;

import be.webtechie.vaadin.pi4j.service.bmp280.BMP280;

/**
 * Event published when new BMP280 sensor readings are available.
 */
public class BMP280Event extends HardwareEvent {

    private final BMP280.Measurement measurement;

    public BMP280Event(Object source, BMP280.Measurement measurement) {
        super(source);
        this.measurement = measurement;
    }

    public BMP280.Measurement getMeasurement() {
        return measurement;
    }

    public double getTemperature() {
        return measurement.temperature();
    }

    public double getPressureHPa() {
        return measurement.pressureHPa();
    }
}
