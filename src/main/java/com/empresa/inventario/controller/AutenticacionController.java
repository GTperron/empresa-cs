package com.empresa.inventario.controller;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.AuthResponse;
import com.empresa.inventario.dto.LoginRequest;
import com.empresa.inventario.dto.RegistroRequest;
import com.empresa.inventario.dto.RefreshTokenRequest;
import com.empresa.inventario.dto.RestablecerContrasenaRequest;
import com.empresa.inventario.dto.SolicitarRecuperacionRequest;
import com.empresa.inventario.service.AutenticacionService;
import com.empresa.inventario.service.RecuperacionContrasenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * Controlador REST para autenticación y autorización.
 */
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación, registro y gestión de tokens")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;
    private final RecuperacionContrasenaService recuperacionContrasenaService;

    /**
     * Registra un nuevo usuario.
     */
    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> registrar(@Valid @RequestBody RegistroRequest request) {
        log.info("Solicitud de registro para email: {}", request.getEmail());
        AuthResponse authResponse = autenticacionService.registrar(request);

        ApiResponse<AuthResponse> respuesta = ApiResponse.<AuthResponse>builder()
                .exitoso(true)
                .mensaje("Usuario registrado exitosamente")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Realiza el login del usuario.
     */
    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica un usuario y devuelve tokens JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Solicitud de login para email: {}", request.getEmail());
        AuthResponse authResponse = autenticacionService.login(request);

        ApiResponse<AuthResponse> respuesta = ApiResponse.<AuthResponse>builder()
                .exitoso(true)
                .mensaje("Login exitoso")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Refresca el access token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Genera un nuevo access token usando el refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refrescado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refrescarToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Solicitud de refresco de token");
        AuthResponse authResponse = autenticacionService.refrescarToken(request);

        ApiResponse<AuthResponse> respuesta = ApiResponse.<AuthResponse>builder()
                .exitoso(true)
                .mensaje("Token refrescado exitosamente")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Solicita recuperación de contraseña.
     */
    @PostMapping("/recuperar-contrasena")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un email con un enlace para recuperar la contraseña")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud procesada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<Void>> solicitarRecuperacion(
            @Valid @RequestBody SolicitarRecuperacionRequest request) {
        log.info("Solicitud de recuperación de contraseña para email: {}", request.getEmail());
        recuperacionContrasenaService.solicitarRecuperacion(request);

        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(true)
                .mensaje("Se ha enviado un correo con instrucciones para recuperar tu contraseña")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Restablece la contraseña con token.
     */
    @PostMapping("/restablecer-contrasena")
    @Operation(summary = "Restablecer contraseña", description = "Restablece la contraseña usando el token de recuperación")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    public ResponseEntity<ApiResponse<Void>> restablecerContrasena(
            @Valid @RequestBody RestablecerContrasenaRequest request) {
        log.info("Solicitud de restablecimiento de contraseña");
        recuperacionContrasenaService.restablecerContrasena(request);

        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(true)
                .mensaje("Contraseña restablecida exitosamente. Ya puedes iniciar sesión con tu nueva contraseña")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }
}
