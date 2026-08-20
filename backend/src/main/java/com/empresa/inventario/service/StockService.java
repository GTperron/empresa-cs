package com.empresa.inventario.service;

import com.empresa.inventario.dto.StockDTO;
import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.Stock;
import com.empresa.inventario.entity.Zona;
import com.empresa.inventario.repository.StockRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de consulta de stock (las modificaciones se hacen vía MovimientoStockService).
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final ProductoService productoService;

    /**
     * Listado paginado de stock con filtros opcionales por producto, estantería y almacén.
     */
    public Page<StockDTO> listar(Long productoId, Long estanteriaId, Long almacenId, Pageable pageable) {
        return stockRepository.buscar(productoId, estanteriaId, almacenId, pageable).map(this::convertirADTO);
    }

    /**
     * Devuelve todas las ubicaciones (estanterías) con stock de un producto.
     */
    public List<StockDTO> listarPorProducto(Long productoId) {
        // Valida existencia del producto (lanza 404 si no existe).
        productoService.obtenerEntidad(productoId);
        return stockRepository.findByProductoId(productoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private StockDTO convertirADTO(Stock stock) {
        Estanteria estanteria = stock.getEstanteria();
        Zona zona = estanteria.getZona();
        return StockDTO.builder()
                .id(stock.getId())
                .productoId(stock.getProducto().getId())
                .productoCodigo(stock.getProducto().getCodigo())
                .productoNombre(stock.getProducto().getNombre())
                .estanteriaId(estanteria.getId())
                .estanteriaCodigo(estanteria.getCodigo())
                .zonaId(zona.getId())
                .zonaCodigo(zona.getCodigo())
                .almacenId(zona.getAlmacen().getId())
                .almacenCodigo(zona.getAlmacen().getCodigo())
                .cantidad(stock.getCantidad())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
