package com.empresa.inventario.service;

import com.empresa.inventario.dto.AjusteRequest;
import com.empresa.inventario.dto.EntradaRequest;
import com.empresa.inventario.dto.MovimientoStockDTO;
import com.empresa.inventario.dto.TrasladoRequest;
import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.MovimientoStock;
import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.entity.Stock;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.enums.MovimientoTipo;
import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.StockInsuficienteException;
import com.empresa.inventario.repository.MovimientoStockRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Servicio que registra movimientos ENTRADA/TRASLADO/AJUSTE.
 * La mutación de stock y el registro del movimiento se delegan en {@link GestorStock},
 * que centraliza el bloqueo pesimista y la lógica de sumar/restar (sin duplicación).
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final GestorStock gestorStock;
    private final ProductoService productoService;
    private final EstanteriaService estanteriaService;
    private final UsuarioService usuarioService;

    /**
     * Registra una ENTRADA: suma cantidad al stock de la estantería (crea la fila si no existe).
     * Solo válido para productos de tipo ENTRADA.
     */
    public MovimientoStockDTO registrarEntrada(EntradaRequest request, Long usuarioId) {
        Producto producto = productoService.obtenerEntidadActiva(request.getProductoId());
        if (producto.getTipo() != ProductoTipo.ENTRADA) {
            throw new OperacionInvalidaException(
                    "Solo se puede registrar una ENTRADA para productos de tipo ENTRADA");
        }
        Estanteria estanteria = estanteriaService.obtenerEntidadActiva(request.getEstanteriaId());
        BigDecimal cantidad = request.getCantidad();

        Map<ClaveStock, Stock> lote = gestorStock.bloquearEnOrden(
                Set.of(new ClaveStock(producto.getId(), estanteria.getId())));
        gestorStock.aplicarDelta(lote, producto, estanteria, cantidad);

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        MovimientoStock movimiento = gestorStock.registrarMovimiento(
                producto, estanteria, null, MovimientoTipo.ENTRADA, cantidad, usuario, request.getMotivo());
        log.info("ENTRADA registrada: producto={}, estanteria={}, cantidad={}, usuario={}",
                producto.getId(), estanteria.getId(), cantidad, usuarioId);
        return convertirADTO(movimiento);
    }

    /**
     * Registra un TRASLADO: resta del origen y suma en el destino (bloqueo ordenado por GestorStock).
     */
    public MovimientoStockDTO registrarTraslado(TrasladoRequest request, Long usuarioId) {
        if (request.getEstanteriaOrigenId().equals(request.getEstanteriaDestinoId())) {
            throw new OperacionInvalidaException(
                    "El origen y el destino no pueden ser la misma estantería");
        }
        Producto producto = productoService.obtenerEntidadActiva(request.getProductoId());
        Estanteria origen = estanteriaService.obtenerEntidadActiva(request.getEstanteriaOrigenId());
        Estanteria destino = estanteriaService.obtenerEntidadActiva(request.getEstanteriaDestinoId());
        BigDecimal cantidad = request.getCantidad();

        Map<ClaveStock, Stock> lote = gestorStock.bloquearEnOrden(Set.of(
                new ClaveStock(producto.getId(), origen.getId()),
                new ClaveStock(producto.getId(), destino.getId())));

        BigDecimal disponible = gestorStock.disponible(lote, producto.getId(), origen.getId());
        if (disponible.compareTo(cantidad) < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente en la estantería de origen: disponible " + disponible
                            + ", solicitado " + cantidad);
        }

        gestorStock.aplicarDelta(lote, producto, origen, cantidad.negate());
        gestorStock.aplicarDelta(lote, producto, destino, cantidad);

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        MovimientoStock movimiento = gestorStock.registrarMovimiento(
                producto, origen, destino, MovimientoTipo.TRASLADO, cantidad, usuario, request.getMotivo());
        log.info("TRASLADO registrado: producto={}, origen={}, destino={}, cantidad={}, usuario={}",
                producto.getId(), origen.getId(), destino.getId(), cantidad, usuarioId);
        return convertirADTO(movimiento);
    }

    /**
     * Registra un AJUSTE (con signo). Requiere motivo y no puede dejar el stock en negativo.
     */
    public MovimientoStockDTO registrarAjuste(AjusteRequest request, Long usuarioId) {
        BigDecimal cantidad = request.getCantidad();
        if (cantidad.compareTo(BigDecimal.ZERO) == 0) {
            throw new OperacionInvalidaException("El ajuste no puede ser cero");
        }
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new OperacionInvalidaException("El motivo es obligatorio para un ajuste");
        }
        Producto producto = productoService.obtenerEntidadActiva(request.getProductoId());
        Estanteria estanteria = estanteriaService.obtenerEntidadActiva(request.getEstanteriaId());

        Map<ClaveStock, Stock> lote = gestorStock.bloquearEnOrden(
                Set.of(new ClaveStock(producto.getId(), estanteria.getId())));
        // aplicarDelta rechaza el resultado negativo (StockInsuficienteException).
        gestorStock.aplicarDelta(lote, producto, estanteria, cantidad);

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        MovimientoStock movimiento = gestorStock.registrarMovimiento(
                producto, estanteria, null, MovimientoTipo.AJUSTE, cantidad, usuario, request.getMotivo());
        log.info("AJUSTE registrado: producto={}, estanteria={}, cantidad={}, usuario={}",
                producto.getId(), estanteria.getId(), cantidad, usuarioId);
        return convertirADTO(movimiento);
    }

    /**
     * Historial paginado de movimientos con filtros opcionales.
     */
    @Transactional(readOnly = true)
    public Page<MovimientoStockDTO> listar(Long productoId, Long estanteriaId, MovimientoTipo tipo,
                                           Long usuarioId, LocalDateTime desde, LocalDateTime hasta,
                                           Pageable pageable) {
        return movimientoStockRepository
                .buscar(productoId, estanteriaId, tipo, usuarioId, desde, hasta, pageable)
                .map(this::convertirADTO);
    }

    private MovimientoStockDTO convertirADTO(MovimientoStock movimiento) {
        Estanteria destino = movimiento.getEstanteriaDestino();
        return MovimientoStockDTO.builder()
                .id(movimiento.getId())
                .productoId(movimiento.getProducto().getId())
                .productoCodigo(movimiento.getProducto().getCodigo())
                .tipo(movimiento.getTipo())
                .estanteriaId(movimiento.getEstanteria().getId())
                .estanteriaCodigo(movimiento.getEstanteria().getCodigo())
                .estanteriaDestinoId(destino != null ? destino.getId() : null)
                .estanteriaDestinoCodigo(destino != null ? destino.getCodigo() : null)
                .cantidad(movimiento.getCantidad())
                .usuarioId(movimiento.getUsuario().getId())
                .usuarioEmail(movimiento.getUsuario().getEmail())
                .motivo(movimiento.getMotivo())
                .fecha(movimiento.getFecha())
                .createdAt(movimiento.getCreatedAt())
                .build();
    }
}
