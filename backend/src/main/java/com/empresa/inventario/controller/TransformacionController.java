package com.empresa.inventario.controller;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.CrearTransformacionRequest;
import com.empresa.inventario.dto.TransformacionDTO;
import com.empresa.inventario.security.UsuarioUserDetails;
import com.empresa.inventario.service.TransformacionService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * Controlador REST para registrar transformaciones (insumo -> productos de venta)
 * y consultar su historial. El usuario se toma del JWT, nunca del body.
 */
@RestController
@RequestMapping("/transformaciones")
@AllArgsConstructor
@Slf4j
@Tag(name = "Transformaciones", description = "Transformación de un insumo (producto ENTRADA) en uno o varios productos de venta")
public class TransformacionController {

    private final TransformacionService transformacionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Registrar transformación",
            description = "Consume un producto de tipo ENTRADA y genera uno o varios productos de tipo VENTA, actualizando el stock de forma atómica.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transformación registrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto o estantería no encontrados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Stock insuficiente del insumo, tipos de producto inválidos, o producto/estantería inactivos")
    })
    public ResponseEntity<ApiResponse<TransformacionDTO>> registrar(
            @Valid @RequestBody CrearTransformacionRequest request) {
        Long usuarioId = obtenerIdDelUsuarioAutenticado();
        log.info("Registrando TRANSFORMACION (usuario={})", usuarioId);
        TransformacionDTO transformacion = transformacionService.registrar(request, usuarioId);

        ApiResponse<TransformacionDTO> respuesta = ApiResponse.<TransformacionDTO>builder()
                .exitoso(true)
                .mensaje("Transformación registrada exitosamente")
                .data(transformacion)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Historial de transformaciones",
            description = "Historial paginado con filtros opcionales por producto de entrada, usuario y rango de fechas.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<ApiResponse<Page<TransformacionDTO>>> listar(
            @RequestParam(required = false) Long productoEntradaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @ParameterObject Pageable pageable) {
        log.debug("Listando transformaciones (productoEntradaId={}, usuarioId={})", productoEntradaId, usuarioId);
        Page<TransformacionDTO> transformaciones =
                transformacionService.listar(productoEntradaId, usuarioId, desde, hasta, pageable);

        ApiResponse<Page<TransformacionDTO>> respuesta = ApiResponse.<Page<TransformacionDTO>>builder()
                .exitoso(true)
                .mensaje("Historial obtenido exitosamente")
                .data(transformaciones)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Detalle de transformación", description = "Detalle completo de una transformación con sus líneas generadas.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transformación obtenida exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transformación no encontrada")
    })
    public ResponseEntity<ApiResponse<TransformacionDTO>> obtenerPorId(@PathVariable Long id) {
        log.debug("Obteniendo transformación id={}", id);
        TransformacionDTO transformacion = transformacionService.obtenerPorId(id);

        ApiResponse<TransformacionDTO> respuesta = ApiResponse.<TransformacionDTO>builder()
                .exitoso(true)
                .mensaje("Transformación obtenida exitosamente")
                .data(transformacion)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    private Long obtenerIdDelUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioUserDetails userDetails = (UsuarioUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
