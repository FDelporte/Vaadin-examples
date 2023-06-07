package be.webtechie.vaadin.pi4j.service.matrix;

public interface MatrixListener {

    void onMatrixSymbolChange(MatrixSymbol symbol);

    void onMatrixDirectionChange(MatrixDirection matrixDirection);
}
