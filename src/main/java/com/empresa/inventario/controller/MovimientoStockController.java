package com.empresa.inventario.controller;

import com.empresa.inventario.dto.AjusteRequest;
import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.EntradaRequest;
import com.empresa.inventario.dto.MovimientoStockDTO;
import com.empresa.inventario.dto.TrasladoRequest;
import com.empresa.inventario.enums.MovimientoTipo;
import com.empresa.inventario.security.UsuarioUserDetails;
import com.empresa.inventario.service.MovimientoStockService;
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
 * Controlador REST para registrar movimientos de stock y consultar el historial.
 * El usuario que ejecuta el movimiento se toma del JWT, nunca del body.
 */
@RestController
@RequestMapping("/movimientos")
@AllArgsConstructor
@Slf4j
@Tag(name = "Movimientos de stock", description = "Registro de entradas, traslados y ajustes; historial de movimientos")
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Registrar entrada", description = "Suma cantidad al stock de una estantería. Solo para productos de tipo ENTRADA.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Entrada registrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto o estantería no encontrados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Producto/estantería inactivos o producto no es de tipo ENTRADA")
    })
    public ResponseEntity<ApiResponse<MovimientoStockDTO>> registrarEntrada(
            @Valid @RequestBody EntradaRequest request) {
        Long usuarioId = obtenerIdDelUsuarioAutenticado();
        log.info("Registrando ENTRADA (usuario={})", usuarioId);
        MovimientoStockDTO movimiento = movimientoStockService.registrarEntrada(request, usuarioId);

        ApiResponse<MovimientoStockDTO> respuesta = ApiResponse.<MovimientoStockDTO>builder()
                .exitoso(true)
                .mensaje("Entrada registrada exitosamente")
                .data(movimiento)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/traslado")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Registrar traslado", description = "Resta del stock de origen y suma en el destino. Falla si no hay stock suficiente o si origen y destino coinciden.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Traslado registrado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto o estantería no encontrados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Stock insuficiente, origen=destino, o producto/estantería inactivos")
    })
    public ResponseEntity<ApiResponse<MovimientoStockDTO>> registrarTraslado(
            @Valid @RequestBody TrasladoRequest request) {
        Long usuarioId = obtenerIdDelUsuarioAutenticado();
        log.info("Registrando TRASLADO (usuario={})", usuarioId);
        MovimientoStockDTO movimiento = movimientoStockService.registrarTraslado(request, usuarioId);

        ApiResponse<MovimientoStockDTO> respuesta = ApiResponse.<MovimientoStockDTO>builder()
                .exitoso(true)
                .mensaje("Traslado registrado exitosamente")
                .data(movimiento)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/ajuste")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar ajuste", description = "Suma o resta stock según el signo de la cantidad. Requiere motivo y rol ADMIN. No puede dejar el stock en negativo.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ajuste registrado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej.: falta el motivo)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto o estantería no encontrados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ajuste dejaría stock negativo, ajuste cero, o producto/estantería inactivos")
    })
    public ResponseEntity<ApiResponse<MovimientoStockDTO>> registrarAjuste(
            @Valid @RequestBody AjusteRequest request) {
        Long usuarioId = obtenerIdDelUsuarioAutenticado();
        log.info("Registrando AJUSTE (usuario={})", usuarioId);
        MovimientoStockDTO movimiento = movimientoStockService.registrarAjuste(request, usuarioId);

        ApiResponse<MovimientoStockDTO> respuesta = ApiResponse.<MovimientoStockDTO>builder()
                .exitoso(true)
                .mensaje("Ajuste registrado exitosamente")
                .data(movimiento)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Historial de movimientos", description = "Historial paginado con filtros opcionales por producto, estantería, tipo, usuario y rango de fechas.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<ApiResponse<Page<MovimientoStockDTO>>> listar(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long estanteriaId,
            @RequestParam(required = false) MovimientoTipo tipo,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @ParameterObject Pageable pageable) {
        log.debug("Listando movimientos (productoId={}, estanteriaId={}, tipo={}, usuarioId={})",
                productoId, estanteriaId, tipo, usuarioId);
        Page<MovimientoStockDTO> movimientos =
                movimientoStockService.listar(productoId, estanteriaId, tipo, usuarioId, desde, hasta, pageable);

        ApiResponse<Page<MovimientoStockDTO>> respuesta = ApiResponse.<Page<MovimientoStockDTO>>builder()
                .exitoso(true)
                .mensaje("Historial obtenido exitosamente")
                .data(movimientos)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene el ID del usuario autenticado desde el contexto de seguridad (JWT).
     */
    private Long obtenerIdDelUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioUserDetails userDetails = (UsuarioUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
