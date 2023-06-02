package be.webtechie.vaadin.pi4j.service.segment;

public interface SevenSegmentListener {

    void onSevenSegmentChange(int position, SevenSegmentSymbol symbol);
}
