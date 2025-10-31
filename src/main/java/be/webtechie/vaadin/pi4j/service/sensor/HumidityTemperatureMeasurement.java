package be.webtechie.vaadin.pi4j.service.sensor;

import java.time.LocalDateTime;

public record HumidityTemperatureMeasurement(LocalDateTime timestamp, double temperature, double humidity) {
    public HumidityTemperatureMeasurement(double temperature, double humidity) {
        this(LocalDateTime.now(), temperature, humidity);
    }

    public static HumidityTemperatureMeasurement random() {
        return new HumidityTemperatureMeasurement(
                Math.random() * 65 - 20,  // Temperature: -20 to +45°C
                Math.random() * 100       // Humidity: 0 to 100%
        );
    }
}