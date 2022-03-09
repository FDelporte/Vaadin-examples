package be.webtechie.vaadin.pi4j.service;

import com.pi4j.io.gpio.digital.DigitalState;

public interface ButtonListener {

    void onButtonEvent(DigitalState state);
}
