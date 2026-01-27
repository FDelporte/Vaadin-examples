package be.webtechie.vaadin.pi4j.service;

public interface ChangeListener {
    <T> void onMessage(ChangeType type, T message);

    enum ChangeType {
        BUZZER,
        DHT11,
        KEY,
        LCD,
        MATRIX,
        OLED,
        SEGMENT,
        SENSOR,
        TOUCH
    }
}
