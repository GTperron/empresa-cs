package com.empresa.inventario.config;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de la aplicación.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de validación de entrada.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errores.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> respuesta = ApiResponse.<Map<String, String>>builder()
                .exitoso(false)
                .mensaje("Error de validación")
                .data(errores)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de recurso no encontrado.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<Void>> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones de recurso ya existe.
     */
    @ExceptionHandler(RecursoYaExisteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleRecursoYaExiste(
            RecursoYaExisteException ex, WebRequest request) {
        log.warn("Recurso ya existe: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.CONFLICT);
    }

    /**
     * Maneja operaciones inválidas por el estado actual del recurso
     * (ej.: desactivar un almacén con zonas activas).
     */
    @ExceptionHandler(OperacionInvalidaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleOperacionInvalida(
            OperacionInvalidaException ex, WebRequest request) {
        log.warn("Operación inválida: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.CONFLICT);
    }

    /**
     * Maneja stock insuficiente / stock negativo en operaciones de movimiento.
     */
    @ExceptionHandler(StockInsuficienteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleStockInsuficiente(
            StockInsuficienteException ex, WebRequest request) {
        log.warn("Stock insuficiente: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.CONFLICT);
    }

    /**
     * Maneja violaciones de integridad de datos (ej.: choque contra una
     * restricción UNIQUE por una condición de carrera que esquivó la validación previa).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Violación de integridad de datos: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje("La operación viola una restricción de integridad de datos (posible duplicado)")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.CONFLICT);
    }

    /**
     * Maneja excepciones de autenticación fallida.
     */
    @ExceptionHandler(AutenticacionFallidaException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<Void>> handleAutenticacionFallida(
            AutenticacionFallidaException ex, WebRequest request) {
        log.warn("Autenticación fallida: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja excepciones de token inválido.
     */
    @ExceptionHandler(TokenInvalidoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<Void>> handleTokenInvalido(
            TokenInvalidoException ex, WebRequest request) {
        log.warn("Token inválido: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja excepciones de acceso denegado.
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<Void>> handleAccesoDenegado(
            AccesoDenegadoException ex, WebRequest request) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja excepciones de autenticación de Spring Security.
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.warn("Error de autenticación: {}", ex.getMessage());
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje("Credenciales inválidas")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja excepciones genéricas no controladas.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(
            Exception ex, WebRequest request) {
        log.error("Error interno del servidor: ", ex);
        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje("Error interno del servidor. Por favor, intenta de nuevo más tarde.")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
