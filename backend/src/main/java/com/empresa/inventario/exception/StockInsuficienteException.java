package com.empresa.inventario.exception;

/**
 * Excepción lanzada cuando una operación dejaría el stock en negativo
 * o no hay stock suficiente en la estantería de origen.
 */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    public StockInsuficienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
