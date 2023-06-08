package be.webtechie.vaadin.pi4j.service.lcd;

import be.webtechie.vaadin.pi4j.service.ChangeListener;

public interface LcdDisplayListener extends ChangeListener {

    void onLcdDisplayChange(int row, String text);
}
