package com.empresa.inventario.controller;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.CrearVentaRequest;
import com.empresa.inventario.dto.VentaDTO;
import com.empresa.inventario.enums.VentaEstado;
import com.empresa.inventario.security.UsuarioUserDetails;
import com.empresa.inventario.service.VentaService;
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
 * Controlador REST para registrar ventas (descuento de stock de productos VENTA)
 * y consultar su historial. El usuario se toma del JWT, nunca del body.
 */
@RestController
@RequestMapping("/ventas")
@AllArgsConstructor
@Slf4j
@Tag(name = "Ventas", description = "Registro de ventas con descuento de stock y cálculo de total en el servidor")
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Registrar venta",
            description = "Descuenta el stock de cada línea, congela el precio del producto y calcula el total en el servidor.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Venta registrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto o estantería no encontrados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Stock insuficiente, producto no es de tipo VENTA, sin precio, o producto/estantería inactivos")
    })
    public ResponseEntity<ApiResponse<VentaDTO>> registrar(@Valid @RequestBody CrearVentaRequest request) {
        Long usuarioId = obtenerIdDelUsuarioAutenticado();
        log.info("Registrando VENTA (usuario={})", usuarioId);
        VentaDTO venta = ventaService.registrar(request, usuarioId);

        ApiResponse<VentaDTO> respuesta = ApiResponse.<VentaDTO>builder()
                .exitoso(true)
                .mensaje("Venta registrada exitosamente")
                .data(venta)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Historial de ventas",
            description = "Historial paginado con filtros opcionales por usuario, estado y rango de fechas.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<ApiResponse<Page<VentaDTO>>> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) VentaEstado estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @ParameterObject Pageable pageable) {
        log.debug("Listando ventas (usuarioId={}, estado={})", usuarioId, estado);
        Page<VentaDTO> ventas = ventaService.listar(usuarioId, estado, desde, hasta, pageable);

        ApiResponse<Page<VentaDTO>> respuesta = ApiResponse.<Page<VentaDTO>>builder()
                .exitoso(true)
                .mensaje("Historial obtenido exitosamente")
                .data(ventas)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Detalle de venta", description = "Detalle completo de una venta con sus líneas.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Venta obtenida exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    public ResponseEntity<ApiResponse<VentaDTO>> obtenerPorId(@PathVariable Long id) {
        log.debug("Obteniendo venta id={}", id);
        VentaDTO venta = ventaService.obtenerPorId(id);

        ApiResponse<VentaDTO> respuesta = ApiResponse.<VentaDTO>builder()
                .exitoso(true)
                .mensaje("Venta obtenida exitosamente")
                .data(venta)
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
