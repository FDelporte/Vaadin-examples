package be.webtechie.vaadin.pi4j.event;

/**
 * Event published when new DHT11 sensor readings are available.
 */
public class DhtMeasurementEvent extends HardwareEvent {

    private final double temperature;
    private final double humidity;

    public DhtMeasurementEvent(Object source, double temperature, double humidity) {
        super(source);
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }
}
