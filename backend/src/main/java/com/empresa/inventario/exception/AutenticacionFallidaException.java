package com.empresa.inventario.exception;

/**
 * Excepción lanzada cuando fallan las credenciales de autenticación.
 */
public class AutenticacionFallidaException extends RuntimeException {

    public AutenticacionFallidaException(String mensaje) {
        super(mensaje);
    }

    public AutenticacionFallidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
