package com.empresa.inventario.controller;

import com.empresa.inventario.dto.AlmacenDTO;
import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.CrearAlmacenRequest;
import com.empresa.inventario.dto.EditarAlmacenRequest;
import com.empresa.inventario.service.AlmacenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * Controlador REST para gestión de almacenes.
 */
@RestController
@RequestMapping("/almacenes")
@AllArgsConstructor
@Slf4j
@Tag(name = "Almacenes", description = "Gestión de almacenes (estructura de almacenamiento)")
public class AlmacenController {

    private final AlmacenService almacenService;

    /**
     * Crea un nuevo almacén (solo ADMIN).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear almacén", description = "Crea un nuevo almacén. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Almacén creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe un almacén con ese código")
    })
    public ResponseEntity<ApiResponse<AlmacenDTO>> crear(@Valid @RequestBody CrearAlmacenRequest request) {
        log.info("Creando almacén con código: {}", request.getCodigo());
        AlmacenDTO almacen = almacenService.crear(request);

        ApiResponse<AlmacenDTO> respuesta = ApiResponse.<AlmacenDTO>builder()
                .exitoso(true)
                .mensaje("Almacén creado exitosamente")
                .data(almacen)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Lista almacenes de forma paginada, con filtro opcional por estado activo (USER y ADMIN).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Listar almacenes", description = "Lista paginada de almacenes, con filtro opcional por estado activo.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<ApiResponse<Page<AlmacenDTO>>> listar(
            @RequestParam(required = false) Boolean activo,
            @ParameterObject Pageable pageable) {
        log.debug("Listando almacenes (activo={})", activo);
        Page<AlmacenDTO> almacenes = almacenService.listar(activo, pageable);

        ApiResponse<Page<AlmacenDTO>> respuesta = ApiResponse.<Page<AlmacenDTO>>builder()
                .exitoso(true)
                .mensaje("Listado obtenido exitosamente")
                .data(almacenes)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene el detalle de un almacén (USER y ADMIN).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Detalle de almacén", description = "Devuelve el detalle de un almacén por ID.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Almacén obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Almacén no encontrado")
    })
    public ResponseEntity<ApiResponse<AlmacenDTO>> obtener(@PathVariable Long id) {
        log.debug("Obteniendo almacén: {}", id);
        AlmacenDTO almacen = almacenService.obtenerPorId(id);

        ApiResponse<AlmacenDTO> respuesta = ApiResponse.<AlmacenDTO>builder()
                .exitoso(true)
                .mensaje("Almacén obtenido exitosamente")
                .data(almacen)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Edita un almacén (solo ADMIN).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Editar almacén", description = "Actualiza los datos de un almacén. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Almacén actualizado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Almacén no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe otro almacén con ese código")
    })
    public ResponseEntity<ApiResponse<AlmacenDTO>> editar(
            @PathVariable Long id, @Valid @RequestBody EditarAlmacenRequest request) {
        log.info("Editando almacén: {}", id);
        AlmacenDTO almacen = almacenService.editar(id, request);

        ApiResponse<AlmacenDTO> respuesta = ApiResponse.<AlmacenDTO>builder()
                .exitoso(true)
                .mensaje("Almacén actualizado exitosamente")
                .data(almacen)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Activa un almacén (solo ADMIN).
     */
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar almacén", description = "Activa un almacén. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Almacén activado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Almacén no encontrado")
    })
    public ResponseEntity<ApiResponse<AlmacenDTO>> activar(@PathVariable Long id) {
        log.info("Activando almacén: {}", id);
        AlmacenDTO almacen = almacenService.activar(id);

        ApiResponse<AlmacenDTO> respuesta = ApiResponse.<AlmacenDTO>builder()
                .exitoso(true)
                .mensaje("Almacén activado exitosamente")
                .data(almacen)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Desactiva un almacén (solo ADMIN). Falla si tiene zonas activas.
     */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar almacén", description = "Desactiva un almacén (soft delete). Falla si tiene zonas activas. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Almacén desactivado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Almacén no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El almacén tiene zonas activas")
    })
    public ResponseEntity<ApiResponse<AlmacenDTO>> desactivar(@PathVariable Long id) {
        log.info("Desactivando almacén: {}", id);
        AlmacenDTO almacen = almacenService.desactivar(id);

        ApiResponse<AlmacenDTO> respuesta = ApiResponse.<AlmacenDTO>builder()
                .exitoso(true)
                .mensaje("Almacén desactivado exitosamente")
                .data(almacen)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }
}
