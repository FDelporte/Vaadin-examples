package be.webtechie.vaadin.pi4j.service;

public interface ChangeListener {
    void onMessage(ChangeType type, String message);

    enum ChangeType {
        BUZZER,
        LCD,
        MATRIX,
        SEGMENT,
        TOUCH
    }
}
