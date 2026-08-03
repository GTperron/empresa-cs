package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearProductoRequest;
import com.empresa.inventario.dto.EditarProductoRequest;
import com.empresa.inventario.dto.ProductoDTO;
import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.exception.RecursoYaExisteException;
import com.empresa.inventario.repository.ProductoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Servicio para gestionar productos.
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;

    /**
     * Crea un producto. El código es único globalmente.
     */
    public ProductoDTO crear(CrearProductoRequest request) {
        if (productoRepository.existsByCodigo(request.getCodigo())) {
            throw new RecursoYaExisteException(
                    "Ya existe un producto con el código '" + request.getCodigo() + "'");
        }
        validarPrecioVenta(request.getTipo(), request.getPrecioVenta());

        Producto producto = Producto.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tipo(request.getTipo())
                .unidadMedida(request.getUnidadMedida())
                .precioVenta(request.getPrecioVenta())
                .activo(true)
                .build();

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado: id={}, codigo={}", guardado.getId(), guardado.getCodigo());
        return convertirADTO(guardado);
    }

    /**
     * Lista productos de forma paginada, con filtros opcionales por tipo y estado activo.
     */
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listar(ProductoTipo tipo, Boolean activo, Pageable pageable) {
        return productoRepository.buscar(tipo, activo, pageable).map(this::convertirADTO);
    }

    /**
     * Obtiene el detalle de un producto por ID.
     */
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        return convertirADTO(obtenerEntidad(id));
    }

    /**
     * Obtiene la entidad Producto o lanza excepción si no existe.
     * Usado internamente y por otros servicios (ej.: MovimientoStockService).
     */
    public Producto obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));
    }

    /**
     * Edita un producto. Revalida la unicidad del código excluyendo el propio registro.
     */
    public ProductoDTO editar(Long id, EditarProductoRequest request) {
        Producto producto = obtenerEntidad(id);

        if (productoRepository.existsByCodigoAndIdNot(request.getCodigo(), id)) {
            throw new RecursoYaExisteException(
                    "Ya existe otro producto con el código '" + request.getCodigo() + "'");
        }
        validarPrecioVenta(request.getTipo(), request.getPrecioVenta());

        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setTipo(request.getTipo());
        producto.setUnidadMedida(request.getUnidadMedida());
        producto.setPrecioVenta(request.getPrecioVenta());

        Producto actualizado = productoRepository.save(producto);
        log.info("Producto actualizado: id={}", actualizado.getId());
        return convertirADTO(actualizado);
    }

    /**
     * Activa un producto.
     */
    public ProductoDTO activar(Long id) {
        Producto producto = obtenerEntidad(id);
        producto.setActivo(true);
        log.info("Producto activado: id={}", id);
        return convertirADTO(productoRepository.save(producto));
    }

    /**
     * Desactiva un producto (soft delete). Los movimientos validan que el producto esté activo.
     */
    public ProductoDTO desactivar(Long id) {
        Producto producto = obtenerEntidad(id);
        producto.setActivo(false);
        log.info("Producto desactivado: id={}", id);
        return convertirADTO(productoRepository.save(producto));
    }

    /**
     * El precio de venta solo aplica a productos de tipo VENTA.
     */
    private void validarPrecioVenta(ProductoTipo tipo, BigDecimal precioVenta) {
        if (tipo == ProductoTipo.ENTRADA && precioVenta != null) {
            throw new OperacionInvalidaException(
                    "El precio de venta solo aplica a productos de tipo VENTA");
        }
    }

    private ProductoDTO convertirADTO(Producto producto) {
        return ProductoDTO.builder()
                .id(producto.getId())
                .codigo(producto.getCodigo())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .tipo(producto.getTipo())
                .unidadMedida(producto.getUnidadMedida())
                .precioVenta(producto.getPrecioVenta())
                .activo(producto.getActivo())
                .createdAt(producto.getCreatedAt())
                .updatedAt(producto.getUpdatedAt())
                .build();
    }
}
