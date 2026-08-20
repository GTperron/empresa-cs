package com.empresa.inventario.exception;

/**
 * Excepción lanzada cuando el usuario no tiene permisos para acceder a un recurso.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }

    public AccesoDenegadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
