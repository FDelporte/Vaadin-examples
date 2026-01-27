package be.webtechie.vaadin.pi4j.service;

public interface ChangeListener {
    <T> void onMessage(ChangeType type, T message);

    enum ChangeType {
        BMP280,
        BUZZER,
        DHT11,
        IR,
        JOYSTICK,
        KEY,
        LCD,
        MATRIX,
        OLED,
        SEGMENT,
        SENSOR,
        TOUCH
    }
}
