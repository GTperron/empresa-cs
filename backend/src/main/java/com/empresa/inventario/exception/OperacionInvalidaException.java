package com.empresa.inventario.exception;

/**
 * Excepción lanzada cuando una operación no es válida por el estado actual del recurso.
 * Ej.: intentar desactivar un almacén que todavía tiene zonas activas.
 */
public class OperacionInvalidaException extends RuntimeException {

    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }

    public OperacionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
