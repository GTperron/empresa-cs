package com.empresa.inventario.controller;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.CrearEstanteriaRequest;
import com.empresa.inventario.dto.EditarEstanteriaRequest;
import com.empresa.inventario.dto.EstanteriaDTO;
import com.empresa.inventario.service.EstanteriaService;
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
 * Controlador REST para gestión de estanterías.
 * Las rutas de creación/listado cuelgan de la zona; el resto operan por ID de estantería.
 */
@RestController
@AllArgsConstructor
@Slf4j
@Tag(name = "Estanterías", description = "Gestión de estanterías dentro de una zona")
public class EstanteriaController {

    private final EstanteriaService estanteriaService;

    /**
     * Crea una estantería dentro de una zona (solo ADMIN).
     */
    @PostMapping("/zonas/{zonaId}/estanterias")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear estantería", description = "Crea una estantería dentro de una zona. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Estantería creada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zona no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Código duplicado en la zona, o zona inactiva")
    })
    public ResponseEntity<ApiResponse<EstanteriaDTO>> crear(
            @PathVariable Long zonaId, @Valid @RequestBody CrearEstanteriaRequest request) {
        log.info("Creando estantería con código {} en zona {}", request.getCodigo(), zonaId);
        EstanteriaDTO estanteria = estanteriaService.crear(zonaId, request);

        ApiResponse<EstanteriaDTO> respuesta = ApiResponse.<EstanteriaDTO>builder()
                .exitoso(true)
                .mensaje("Estantería creada exitosamente")
                .data(estanteria)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Lista las estanterías de una zona (USER y ADMIN).
     */
    @GetMapping("/zonas/{zonaId}/estanterias")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Listar estanterías por zona", description = "Lista las estanterías de una zona, con filtro opcional por estado activo.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zona no encontrada")
    })
    public ResponseEntity<ApiResponse<List<EstanteriaDTO>>> listarPorZona(
            @PathVariable Long zonaId,
            @RequestParam(required = false) Boolean activo) {
        log.debug("Listando estanterías de la zona {} (activo={})", zonaId, activo);
        List<EstanteriaDTO> estanterias = estanteriaService.listarPorZona(zonaId, activo);

        ApiResponse<List<EstanteriaDTO>> respuesta = ApiResponse.<List<EstanteriaDTO>>builder()
                .exitoso(true)
                .mensaje("Listado obtenido exitosamente")
                .data(estanterias)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene el detalle de una estantería (USER y ADMIN).
     */
    @GetMapping("/estanterias/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Detalle de estantería", description = "Devuelve el detalle de una estantería por ID.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estantería obtenida exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Estantería no encontrada")
    })
    public ResponseEntity<ApiResponse<EstanteriaDTO>> obtener(@PathVariable Long id) {
        log.debug("Obteniendo estantería: {}", id);
        EstanteriaDTO estanteria = estanteriaService.obtenerPorId(id);

        ApiResponse<EstanteriaDTO> respuesta = ApiResponse.<EstanteriaDTO>builder()
                .exitoso(true)
                .mensaje("Estantería obtenida exitosamente")
                .data(estanteria)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Edita una estantería (solo ADMIN).
     */
    @PutMapping("/estanterias/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Editar estantería", description = "Actualiza los datos de una estantería. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estantería actualizada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Estantería no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe otra estantería con ese código en la zona")
    })
    public ResponseEntity<ApiResponse<EstanteriaDTO>> editar(
            @PathVariable Long id, @Valid @RequestBody EditarEstanteriaRequest request) {
        log.info("Editando estantería: {}", id);
        EstanteriaDTO estanteria = estanteriaService.editar(id, request);

        ApiResponse<EstanteriaDTO> respuesta = ApiResponse.<EstanteriaDTO>builder()
                .exitoso(true)
                .mensaje("Estantería actualizada exitosamente")
                .data(estanteria)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Activa una estantería (solo ADMIN).
     */
    @PatchMapping("/estanterias/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar estantería", description = "Activa una estantería. Falla si su zona está inactiva. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estantería activada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Estantería no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "La zona de la estantería está inactiva")
    })
    public ResponseEntity<ApiResponse<EstanteriaDTO>> activar(@PathVariable Long id) {
        log.info("Activando estantería: {}", id);
        EstanteriaDTO estanteria = estanteriaService.activar(id);

        ApiResponse<EstanteriaDTO> respuesta = ApiResponse.<EstanteriaDTO>builder()
                .exitoso(true)
                .mensaje("Estantería activada exitosamente")
                .data(estanteria)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Desactiva una estantería (solo ADMIN).
     */
    @PatchMapping("/estanterias/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar estantería", description = "Desactiva una estantería (soft delete). Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estantería desactivada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Estantería no encontrada")
    })
    public ResponseEntity<ApiResponse<EstanteriaDTO>> desactivar(@PathVariable Long id) {
        log.info("Desactivando estantería: {}", id);
        EstanteriaDTO estanteria = estanteriaService.desactivar(id);

        ApiResponse<EstanteriaDTO> respuesta = ApiResponse.<EstanteriaDTO>builder()
                .exitoso(true)
                .mensaje("Estantería desactivada exitosamente")
                .data(estanteria)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }
}
