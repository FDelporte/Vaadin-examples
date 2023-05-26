package be.webtechie.vaadin.pi4j.service.touch;

import com.pi4j.io.gpio.digital.DigitalState;

public interface TouchListener {

    void onTouchEvent(DigitalState state);
}
