package be.webtechie.vaadin.pi4j.service.matrix;

import be.webtechie.vaadin.pi4j.service.ChangeListener;

public interface MatrixListener extends ChangeListener {

    void onMatrixSymbolChange(MatrixSymbol symbol);

    void onMatrixDirectionChange(MatrixDirection matrixDirection);
}
