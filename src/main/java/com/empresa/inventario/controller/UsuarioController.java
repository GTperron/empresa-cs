package com.empresa.inventario.controller;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.CambiarContrasenaRequest;
import com.empresa.inventario.dto.EditarPerfilRequest;
import com.empresa.inventario.dto.RefreshTokenRequest;
import com.empresa.inventario.dto.UsuarioDTO;
import com.empresa.inventario.service.AutenticacionService;
import com.empresa.inventario.service.UsuarioService;
import com.empresa.inventario.security.UsuarioUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * Controlador REST para gestión de usuarios.
 */
@RestController
@RequestMapping("/usuarios")
@AllArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Endpoints para gestión de perfil y datos de usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AutenticacionService autenticacionService;

    /**
     * Obtiene el perfil del usuario autenticado.
     */
    @GetMapping("/perfil")
    @Operation(summary = "Obtener perfil del usuario", description = "Devuelve la información del usuario autenticado")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<UsuarioDTO>> obtenerPerfil() {
        Long usuarioId = obtenerIdDelUsuarioAutenticado();
        log.info("Obteniendo perfil para usuario: {}", usuarioId);

        UsuarioDTO usuarioDTO = usuarioService.obtenerPerfil(usuarioId);

        ApiResponse<UsuarioDTO> respuesta = ApiResponse.<UsuarioDTO>builder()
                .exitoso(true)
                .mensaje("Perfil obtenido exitosamente")
                .data(usuarioDTO)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Edita el perfil del usuario autenticado.
     */
    @PutMapping("/perfil")
    @Operation(summary = "Editar perfil del usuario", description = "Actualiza la información del perfil del usuario autenticado")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<UsuarioDTO>> editarPerfil(
            @Valid @RequestBody EditarPerfilRequest request) {
        Long usuarioId = obtenerIdDelUsuarioAutenticado();
        log.info("Editando perfil para usuario: {}", usuarioId);

        UsuarioDTO usuarioDTO = usuarioService.editarPerfil(usuarioId, request);

        ApiResponse<UsuarioDTO> respuesta = ApiResponse.<UsuarioDTO>builder()
                .exitoso(true)
                .mensaje("Perfil actualizado exitosamente")
                .data(usuarioDTO)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     */
    @PutMapping("/cambiar-contrasena")
    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña del usuario autenticado")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado o contraseña actual inválida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponse<Void>> cambiarContrasena(
            @Valid @RequestBody CambiarContrasenaRequest request) {
        Long usuarioId = obtenerIdDelUsuarioAutenticado();
        log.info("Cambiando contraseña para usuario: {}", usuarioId);

        autenticacionService.cambiarContrasena(usuarioId, request);

        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(true)
                .mensaje("Contraseña cambiada exitosamente. Por favor, inicia sesión de nuevo")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Realiza logout revocando el refresh token.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Cierra la sesión revocando el refresh token")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout exitoso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout para usuario");
        autenticacionService.logout(request.getRefreshToken());

        ApiResponse<Void> respuesta = ApiResponse.<Void>builder()
                .exitoso(true)
                .mensaje("Logout exitoso")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene el ID del usuario autenticado desde el contexto de seguridad.
     */
    private Long obtenerIdDelUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioUserDetails userDetails = (UsuarioUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}