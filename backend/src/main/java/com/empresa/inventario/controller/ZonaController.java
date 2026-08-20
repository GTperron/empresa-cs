package com.empresa.inventario.controller;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.CrearZonaRequest;
import com.empresa.inventario.dto.EditarZonaRequest;
import com.empresa.inventario.dto.ZonaDTO;
import com.empresa.inventario.service.ZonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestión de zonas.
 * Las rutas de creación/listado cuelgan del almacén; el resto operan por ID de zona.
 */
@RestController
@AllArgsConstructor
@Slf4j
@Tag(name = "Zonas", description = "Gestión de zonas dentro de un almacén")
public class ZonaController {

    private final ZonaService zonaService;

    /**
     * Crea una zona dentro de un almacén (solo ADMIN).
     */
    @PostMapping("/almacenes/{almacenId}/zonas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear zona", description = "Crea una zona dentro de un almacén. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Zona creada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Almacén no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Código duplicado en el almacén, o almacén inactivo")
    })
    public ResponseEntity<ApiResponse<ZonaDTO>> crear(
            @PathVariable Long almacenId, @Valid @RequestBody CrearZonaRequest request) {
        log.info("Creando zona con código {} en almacén {}", request.getCodigo(), almacenId);
        ZonaDTO zona = zonaService.crear(almacenId, request);

        ApiResponse<ZonaDTO> respuesta = ApiResponse.<ZonaDTO>builder()
                .exitoso(true)
                .mensaje("Zona creada exitosamente")
                .data(zona)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Lista las zonas de un almacén (USER y ADMIN).
     */
    @GetMapping("/almacenes/{almacenId}/zonas")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Listar zonas por almacén", description = "Lista las zonas de un almacén, con filtro opcional por estado activo.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Almacén no encontrado")
    })
    public ResponseEntity<ApiResponse<List<ZonaDTO>>> listarPorAlmacen(
            @PathVariable Long almacenId,
            @RequestParam(required = false) Boolean activo) {
        log.debug("Listando zonas del almacén {} (activo={})", almacenId, activo);
        List<ZonaDTO> zonas = zonaService.listarPorAlmacen(almacenId, activo);

        ApiResponse<List<ZonaDTO>> respuesta = ApiResponse.<List<ZonaDTO>>builder()
                .exitoso(true)
                .mensaje("Listado obtenido exitosamente")
                .data(zonas)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene el detalle de una zona (USER y ADMIN).
     */
    @GetMapping("/zonas/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Detalle de zona", description = "Devuelve el detalle de una zona por ID.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Zona obtenida exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zona no encontrada")
    })
    public ResponseEntity<ApiResponse<ZonaDTO>> obtener(@PathVariable Long id) {
        log.debug("Obteniendo zona: {}", id);
        ZonaDTO zona = zonaService.obtenerPorId(id);

        ApiResponse<ZonaDTO> respuesta = ApiResponse.<ZonaDTO>builder()
                .exitoso(true)
                .mensaje("Zona obtenida exitosamente")
                .data(zona)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Edita una zona (solo ADMIN).
     */
    @PutMapping("/zonas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Editar zona", description = "Actualiza los datos de una zona. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Zona actualizada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zona no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe otra zona con ese código en el almacén")
    })
    public ResponseEntity<ApiResponse<ZonaDTO>> editar(
            @PathVariable Long id, @Valid @RequestBody EditarZonaRequest request) {
        log.info("Editando zona: {}", id);
        ZonaDTO zona = zonaService.editar(id, request);

        ApiResponse<ZonaDTO> respuesta = ApiResponse.<ZonaDTO>builder()
                .exitoso(true)
                .mensaje("Zona actualizada exitosamente")
                .data(zona)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Activa una zona (solo ADMIN).
     */
    @PatchMapping("/zonas/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar zona", description = "Activa una zona. Falla si su almacén está inactivo. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Zona activada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zona no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El almacén de la zona está inactivo")
    })
    public ResponseEntity<ApiResponse<ZonaDTO>> activar(@PathVariable Long id) {
        log.info("Activando zona: {}", id);
        ZonaDTO zona = zonaService.activar(id);

        ApiResponse<ZonaDTO> respuesta = ApiResponse.<ZonaDTO>builder()
                .exitoso(true)
                .mensaje("Zona activada exitosamente")
                .data(zona)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Desactiva una zona (solo ADMIN). Falla si tiene estanterías activas.
     */
    @PatchMapping("/zonas/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar zona", description = "Desactiva una zona (soft delete). Falla si tiene estanterías activas. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Zona desactivada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zona no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "La zona tiene estanterías activas")
    })
    public ResponseEntity<ApiResponse<ZonaDTO>> desactivar(@PathVariable Long id) {
        log.info("Desactivando zona: {}", id);
        ZonaDTO zona = zonaService.desactivar(id);

        ApiResponse<ZonaDTO> respuesta = ApiResponse.<ZonaDTO>builder()
                .exitoso(true)
                .mensaje("Zona desactivada exitosamente")
                .data(zona)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }
}
