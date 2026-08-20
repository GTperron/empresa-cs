package com.empresa.inventario.controller;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.StockDTO;
import com.empresa.inventario.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para consulta de stock.
 */
@RestController
@RequestMapping("/stock")
@AllArgsConstructor
@Slf4j
@Tag(name = "Stock", description = "Consulta de existencias por producto y ubicación")
public class StockController {

    private final StockService stockService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Listar stock", description = "Listado paginado de stock con filtros opcionales por producto, estantería y almacén.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<ApiResponse<Page<StockDTO>>> listar(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long estanteriaId,
            @RequestParam(required = false) Long almacenId,
            @ParameterObject Pageable pageable) {
        log.debug("Listando stock (productoId={}, estanteriaId={}, almacenId={})",
                productoId, estanteriaId, almacenId);
        Page<StockDTO> stock = stockService.listar(productoId, estanteriaId, almacenId, pageable);

        ApiResponse<Page<StockDTO>> respuesta = ApiResponse.<Page<StockDTO>>builder()
                .exitoso(true)
                .mensaje("Listado obtenido exitosamente")
                .data(stock)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Stock por producto", description = "Todas las ubicaciones (estanterías) donde hay stock de un producto, con la cantidad en cada una.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ApiResponse<List<StockDTO>>> listarPorProducto(@PathVariable Long productoId) {
        log.debug("Listando stock del producto: {}", productoId);
        List<StockDTO> stock = stockService.listarPorProducto(productoId);

        ApiResponse<List<StockDTO>> respuesta = ApiResponse.<List<StockDTO>>builder()
                .exitoso(true)
                .mensaje("Listado obtenido exitosamente")
                .data(stock)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }
}
