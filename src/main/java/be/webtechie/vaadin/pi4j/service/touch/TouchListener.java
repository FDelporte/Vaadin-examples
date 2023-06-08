package be.webtechie.vaadin.pi4j.service.touch;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import com.pi4j.io.gpio.digital.DigitalState;

public interface TouchListener extends ChangeListener {

    void onTouchEvent(DigitalState state);
}
