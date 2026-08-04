package com.empresa.inventario.service;

import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.MovimientoStock;
import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.entity.Stock;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.enums.MovimientoTipo;
import com.empresa.inventario.exception.StockInsuficienteException;
import com.empresa.inventario.repository.MovimientoStockRepository;
import com.empresa.inventario.repository.StockRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Componente compartido que centraliza la mutación atómica de stock y el registro de
 * movimientos. Única fuente de verdad del bloqueo pesimista y del "sumar/restar con
 * creación de fila", reutilizado por movimientos (ENTRADA/TRASLADO/AJUSTE),
 * transformaciones y ventas. No debe invocarse fuera de una transacción.
 */
@Service
@AllArgsConstructor
@Slf4j
public class GestorStock {

    private final StockRepository stockRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    /**
     * Bloquea (SELECT ... FOR UPDATE) todas las filas de las claves dadas en orden
     * determinístico (por estantería y luego producto), para evitar deadlocks cuando
     * hay N filas involucradas. Devuelve un mapa clave -> fila (null si aún no existe).
     */
    public Map<ClaveStock, Stock> bloquearEnOrden(Set<ClaveStock> claves) {
        Map<ClaveStock, Stock> lote = new LinkedHashMap<>();
        claves.stream().sorted().forEach(clave ->
                lote.put(clave, stockRepository
                        .lockByProductoAndEstanteria(clave.productoId(), clave.estanteriaId())
                        .orElse(null)));
        return lote;
    }

    /**
     * Cantidad disponible de una clave dentro del lote ya bloqueado (0 si la fila no existe).
     */
    public BigDecimal disponible(Map<ClaveStock, Stock> lote, Long productoId, Long estanteriaId) {
        Stock stock = lote.get(new ClaveStock(productoId, estanteriaId));
        return stock == null ? BigDecimal.ZERO : stock.getCantidad();
    }

    /**
     * Aplica un delta (con signo) sobre la fila ya bloqueada; crea la fila si no existía
     * y acumula sobre la misma referencia si la clave se toca más de una vez. Rechaza el
     * resultado negativo. Es la única implementación de "sumar/restar stock".
     */
    public Stock aplicarDelta(Map<ClaveStock, Stock> lote, Producto producto, Estanteria estanteria, BigDecimal delta) {
        ClaveStock clave = new ClaveStock(producto.getId(), estanteria.getId());
        Stock stock = lote.get(clave);
        if (stock == null) {
            stock = Stock.builder()
                    .producto(producto)
                    .estanteria(estanteria)
                    .cantidad(BigDecimal.ZERO)
                    .build();
            lote.put(clave, stock);
        }

        BigDecimal nueva = stock.getCantidad().add(delta);
        if (nueva.compareTo(BigDecimal.ZERO) < 0) {
            throw new StockInsuficienteException(
                    "El movimiento dejaría el stock en negativo para el producto '" + producto.getCodigo()
                            + "' en la estantería '" + estanteria.getCodigo()
                            + "': actual " + stock.getCantidad() + ", delta " + delta);
        }
        stock.setCantidad(nueva);
        return stockRepository.save(stock);
    }

    /**
     * Registra un movimiento de stock. Única implementación de creación de movimientos.
     */
    public MovimientoStock registrarMovimiento(Producto producto, Estanteria estanteriaOrigen,
                                               Estanteria estanteriaDestino, MovimientoTipo tipo,
                                               BigDecimal cantidad, Usuario usuario, String motivo) {
        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(producto)
                .estanteria(estanteriaOrigen)
                .estanteriaDestino(estanteriaDestino)
                .tipo(tipo)
                .cantidad(cantidad)
                .usuario(usuario)
                .motivo(motivo)
                .fecha(LocalDateTime.now())
                .build();
        return movimientoStockRepository.save(movimiento);
    }
}
