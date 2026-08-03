package com.empresa.inventario.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe.
 */
public class RecursoYaExisteException extends RuntimeException {

    public RecursoYaExisteException(String mensaje) {
        super(mensaje);
    }

    public RecursoYaExisteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
