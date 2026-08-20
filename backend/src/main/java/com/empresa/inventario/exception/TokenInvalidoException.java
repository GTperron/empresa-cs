package com.empresa.inventario.exception;

/**
 * Excepción lanzada cuando un token JWT es inválido o ha expirado.
 */
public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }

    public TokenInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
