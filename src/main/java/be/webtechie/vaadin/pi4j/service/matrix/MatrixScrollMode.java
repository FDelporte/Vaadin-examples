package be.webtechie.vaadin.pi4j.service.matrix;

public enum MatrixScrollMode {
    /**
     * Normally scroll the LED matrix in one direction, causing one row or column to be empty.
     */
    NORMAL,
    /**
     * Scroll the LED matrix in one direction and wrap the row or column around to the other side.
     */
    ROTATE,
    /**
     * Scroll the LED matrix in one direction and replace the now empty row or column with values from a new buffer.
     * This can be used to gradually transition from one buffer to another via scrolling.
     */
    REPLACE
}
