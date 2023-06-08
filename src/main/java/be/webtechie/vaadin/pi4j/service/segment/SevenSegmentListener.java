package be.webtechie.vaadin.pi4j.service.segment;

import be.webtechie.vaadin.pi4j.service.ChangeListener;

public interface SevenSegmentListener extends ChangeListener {

    void onSevenSegmentChange(int position, SevenSegmentSymbol symbol);
}
