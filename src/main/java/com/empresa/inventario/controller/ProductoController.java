package com.empresa.inventario.controller;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.dto.CrearProductoRequest;
import com.empresa.inventario.dto.EditarProductoRequest;
import com.empresa.inventario.dto.ProductoDTO;
import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.service.ProductoService;
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
 * Controlador REST para gestión de productos.
 */
@RestController
@RequestMapping("/productos")
@AllArgsConstructor
@Slf4j
@Tag(name = "Productos", description = "Gestión de productos del inventario")
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Código duplicado o precio de venta inválido para el tipo")
    })
    public ResponseEntity<ApiResponse<ProductoDTO>> crear(@Valid @RequestBody CrearProductoRequest request) {
        log.info("Creando producto con código: {}", request.getCodigo());
        ProductoDTO producto = productoService.crear(request);

        ApiResponse<ProductoDTO> respuesta = ApiResponse.<ProductoDTO>builder()
                .exitoso(true)
                .mensaje("Producto creado exitosamente")
                .data(producto)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Listar productos", description = "Lista paginada de productos, con filtros opcionales por tipo y estado activo.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<ApiResponse<Page<ProductoDTO>>> listar(
            @RequestParam(required = false) ProductoTipo tipo,
            @RequestParam(required = false) Boolean activo,
            @ParameterObject Pageable pageable) {
        log.debug("Listando productos (tipo={}, activo={})", tipo, activo);
        Page<ProductoDTO> productos = productoService.listar(tipo, activo, pageable);

        ApiResponse<Page<ProductoDTO>> respuesta = ApiResponse.<Page<ProductoDTO>>builder()
                .exitoso(true)
                .mensaje("Listado obtenido exitosamente")
                .data(productos)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Detalle de producto", description = "Devuelve el detalle de un producto por ID.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ApiResponse<ProductoDTO>> obtener(@PathVariable Long id) {
        log.debug("Obteniendo producto: {}", id);
        ProductoDTO producto = productoService.obtenerPorId(id);

        ApiResponse<ProductoDTO> respuesta = ApiResponse.<ProductoDTO>builder()
                .exitoso(true)
                .mensaje("Producto obtenido exitosamente")
                .data(producto)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Editar producto", description = "Actualiza los datos de un producto. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Código duplicado o precio de venta inválido para el tipo")
    })
    public ResponseEntity<ApiResponse<ProductoDTO>> editar(
            @PathVariable Long id, @Valid @RequestBody EditarProductoRequest request) {
        log.info("Editando producto: {}", id);
        ProductoDTO producto = productoService.editar(id, request);

        ApiResponse<ProductoDTO> respuesta = ApiResponse.<ProductoDTO>builder()
                .exitoso(true)
                .mensaje("Producto actualizado exitosamente")
                .data(producto)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar producto", description = "Activa un producto. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto activado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ApiResponse<ProductoDTO>> activar(@PathVariable Long id) {
        log.info("Activando producto: {}", id);
        ProductoDTO producto = productoService.activar(id);

        ApiResponse<ProductoDTO> respuesta = ApiResponse.<ProductoDTO>builder()
                .exitoso(true)
                .mensaje("Producto activado exitosamente")
                .data(producto)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar producto", description = "Desactiva un producto (soft delete). Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto desactivado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Prohibido (requiere rol ADMIN)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ApiResponse<ProductoDTO>> desactivar(@PathVariable Long id) {
        log.info("Desactivando producto: {}", id);
        ProductoDTO producto = productoService.desactivar(id);

        ApiResponse<ProductoDTO> respuesta = ApiResponse.<ProductoDTO>builder()
                .exitoso(true)
                .mensaje("Producto desactivado exitosamente")
                .data(producto)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(respuesta);
    }
}
