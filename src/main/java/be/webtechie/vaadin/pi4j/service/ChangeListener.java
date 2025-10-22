package be.webtechie.vaadin.pi4j.service;

public interface ChangeListener {
    <T> void onMessage(ChangeType type, T message);

    enum ChangeType {
        BUZZER,
        DHT11,
        LCD,
        MATRIX,
        SEGMENT,
        SENSOR,
        TOUCH
    }
}
