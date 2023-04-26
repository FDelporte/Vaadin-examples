package be.webtechie.vaadin.pi4j.service.matrix;

public interface MatrixListener {

    void onMatrixChange(MatrixSymbol symbol, MatrixDirection matrixDirection);
}
