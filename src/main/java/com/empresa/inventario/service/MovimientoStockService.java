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
import com.empresa.inventario.repository.StockRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio que registra movimientos de stock y actualiza las cantidades de forma atómica.
 *
 * <p>Estrategia de concurrencia: cada operación de escritura corre dentro de una única
 * transacción y toma un lock pesimista de escritura (SELECT ... FOR UPDATE) sobre la(s)
 * fila(s) de stock afectadas. Para el TRASLADO se bloquean las dos filas en orden
 * determinístico (por id de estantería ascendente) para evitar deadlocks. Si algo falla,
 * el rollback deshace tanto el movimiento como la actualización de stock.</p>
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final StockRepository stockRepository;
    private final ProductoService productoService;
    private final EstanteriaService estanteriaService;
    private final UsuarioService usuarioService;

    /**
     * Registra una ENTRADA: suma cantidad al stock de la estantería (crea la fila si no existe).
     * Solo válido para productos de tipo ENTRADA.
     */
    public MovimientoStockDTO registrarEntrada(EntradaRequest request, Long usuarioId) {
        Producto producto = obtenerProductoActivo(request.getProductoId());
        if (producto.getTipo() != ProductoTipo.ENTRADA) {
            throw new OperacionInvalidaException(
                    "Solo se puede registrar una ENTRADA para productos de tipo ENTRADA");
        }
        Estanteria estanteria = obtenerEstanteriaActiva(request.getEstanteriaId());
        BigDecimal cantidad = request.getCantidad();

        sumarStock(producto, estanteria, cantidad);

        MovimientoStock movimiento = guardarMovimiento(
                producto, estanteria, null, MovimientoTipo.ENTRADA, cantidad, usuarioId, request.getMotivo());
        log.info("ENTRADA registrada: producto={}, estanteria={}, cantidad={}, usuario={}",
                producto.getId(), estanteria.getId(), cantidad, usuarioId);
        return convertirADTO(movimiento);
    }

    /**
     * Registra un TRASLADO: resta del origen y suma en el destino (bloqueo ordenado).
     */
    public MovimientoStockDTO registrarTraslado(TrasladoRequest request, Long usuarioId) {
        if (request.getEstanteriaOrigenId().equals(request.getEstanteriaDestinoId())) {
            throw new OperacionInvalidaException(
                    "El origen y el destino no pueden ser la misma estantería");
        }
        Producto producto = obtenerProductoActivo(request.getProductoId());
        Estanteria origen = obtenerEstanteriaActiva(request.getEstanteriaOrigenId());
        Estanteria destino = obtenerEstanteriaActiva(request.getEstanteriaDestinoId());
        BigDecimal cantidad = request.getCantidad();

        // Lock ordering: bloquear siempre por id ascendente para evitar deadlocks.
        Long menorId = Math.min(origen.getId(), destino.getId());
        Long mayorId = Math.max(origen.getId(), destino.getId());
        Optional<Stock> lockMenor = stockRepository.lockByProductoAndEstanteria(producto.getId(), menorId);
        Optional<Stock> lockMayor = stockRepository.lockByProductoAndEstanteria(producto.getId(), mayorId);

        Optional<Stock> stockOrigen = origen.getId().equals(menorId) ? lockMenor : lockMayor;
        Optional<Stock> stockDestino = destino.getId().equals(menorId) ? lockMenor : lockMayor;

        BigDecimal disponible = stockOrigen.map(Stock::getCantidad).orElse(BigDecimal.ZERO);
        if (disponible.compareTo(cantidad) < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente en la estantería de origen: disponible " + disponible
                            + ", solicitado " + cantidad);
        }

        // Restar del origen (garantizado no negativo por el chequeo anterior).
        Stock origenStock = stockOrigen.get();
        origenStock.setCantidad(origenStock.getCantidad().subtract(cantidad));

        // Sumar en el destino (crear la fila si no existía).
        if (stockDestino.isPresent()) {
            Stock d = stockDestino.get();
            d.setCantidad(d.getCantidad().add(cantidad));
        } else {
            crearStock(producto, destino, cantidad);
        }

        MovimientoStock movimiento = guardarMovimiento(
                producto, origen, destino, MovimientoTipo.TRASLADO, cantidad, usuarioId, request.getMotivo());
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
        Producto producto = obtenerProductoActivo(request.getProductoId());
        Estanteria estanteria = obtenerEstanteriaActiva(request.getEstanteriaId());

        Optional<Stock> existente =
                stockRepository.lockByProductoAndEstanteria(producto.getId(), estanteria.getId());
        BigDecimal actual = existente.map(Stock::getCantidad).orElse(BigDecimal.ZERO);
        BigDecimal nueva = actual.add(cantidad);
        if (nueva.compareTo(BigDecimal.ZERO) < 0) {
            throw new StockInsuficienteException(
                    "El ajuste dejaría el stock en negativo: actual " + actual + ", ajuste " + cantidad);
        }

        if (existente.isPresent()) {
            existente.get().setCantidad(nueva);
        } else {
            crearStock(producto, estanteria, nueva);
        }

        MovimientoStock movimiento = guardarMovimiento(
                producto, estanteria, null, MovimientoTipo.AJUSTE, cantidad, usuarioId, request.getMotivo());
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

    // --- Helpers ---

    private Producto obtenerProductoActivo(Long productoId) {
        Producto producto = productoService.obtenerEntidad(productoId);
        if (!producto.getActivo()) {
            throw new OperacionInvalidaException("El producto está inactivo");
        }
        return producto;
    }

    private Estanteria obtenerEstanteriaActiva(Long estanteriaId) {
        Estanteria estanteria = estanteriaService.obtenerEntidad(estanteriaId);
        if (!estanteria.getActivo()) {
            throw new OperacionInvalidaException("La estantería " + estanteriaId + " está inactiva");
        }
        return estanteria;
    }

    /**
     * Suma delta al stock de (producto, estantería) tomando lock; crea la fila si no existe.
     * La unicidad (producto, estantería) en BD respalda el caso de creación concurrente.
     */
    private void sumarStock(Producto producto, Estanteria estanteria, BigDecimal delta) {
        Optional<Stock> existente =
                stockRepository.lockByProductoAndEstanteria(producto.getId(), estanteria.getId());
        if (existente.isPresent()) {
            Stock stock = existente.get();
            stock.setCantidad(stock.getCantidad().add(delta));
        } else {
            crearStock(producto, estanteria, delta);
        }
    }

    private void crearStock(Producto producto, Estanteria estanteria, BigDecimal cantidad) {
        Stock stock = Stock.builder()
                .producto(producto)
                .estanteria(estanteria)
                .cantidad(cantidad)
                .build();
        stockRepository.save(stock);
    }

    private MovimientoStock guardarMovimiento(Producto producto, Estanteria estanteria,
                                              Estanteria estanteriaDestino, MovimientoTipo tipo,
                                              BigDecimal cantidad, Long usuarioId, String motivo) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(producto)
                .estanteria(estanteria)
                .estanteriaDestino(estanteriaDestino)
                .tipo(tipo)
                .cantidad(cantidad)
                .usuario(usuario)
                .motivo(motivo)
                .fecha(LocalDateTime.now())
                .build();
        return movimientoStockRepository.save(movimiento);
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
