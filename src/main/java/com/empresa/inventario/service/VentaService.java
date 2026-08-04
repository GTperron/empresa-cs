package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearVentaDetalleRequest;
import com.empresa.inventario.dto.CrearVentaRequest;
import com.empresa.inventario.dto.VentaDTO;
import com.empresa.inventario.dto.VentaDetalleDTO;
import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.entity.Stock;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.entity.Venta;
import com.empresa.inventario.entity.VentaDetalle;
import com.empresa.inventario.enums.MovimientoTipo;
import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.enums.VentaEstado;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.exception.StockInsuficienteException;
import com.empresa.inventario.repository.VentaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio que registra ventas: descuenta stock de cada línea de forma atómica vía
 * GestorStock, calcula el total en el servidor y deja trazabilidad de SALIDA_VENTA.
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class VentaService {

    private final VentaRepository ventaRepository;
    private final GestorStock gestorStock;
    private final ProductoService productoService;
    private final EstanteriaService estanteriaService;
    private final UsuarioService usuarioService;

    public VentaDTO registrar(CrearVentaRequest request, Long usuarioId) {
        // 1) Validar cada línea (producto VENTA activo con precio + estantería activa).
        List<LineaVenta> lineas = request.getDetalles().stream()
                .map(this::validarLinea)
                .collect(Collectors.toList());

        // 2) Bloquear en orden todas las filas de stock involucradas (dedup por clave).
        Set<ClaveStock> claves = new HashSet<>();
        lineas.forEach(l -> claves.add(new ClaveStock(l.producto().getId(), l.estanteria().getId())));
        Map<ClaveStock, Stock> lote = gestorStock.bloquearEnOrden(claves);

        // 3) Validar stock suficiente por clave (acumulando líneas repetidas).
        Map<ClaveStock, BigDecimal> requeridoPorClave = new HashMap<>();
        for (LineaVenta l : lineas) {
            ClaveStock clave = new ClaveStock(l.producto().getId(), l.estanteria().getId());
            requeridoPorClave.merge(clave, l.cantidad(), BigDecimal::add);
        }
        for (LineaVenta l : lineas) {
            ClaveStock clave = new ClaveStock(l.producto().getId(), l.estanteria().getId());
            BigDecimal requerido = requeridoPorClave.get(clave);
            BigDecimal disponible = gestorStock.disponible(lote, clave.productoId(), clave.estanteriaId());
            if (disponible.compareTo(requerido) < 0) {
                throw new StockInsuficienteException(
                        "Stock insuficiente del producto '" + l.producto().getCodigo()
                                + "' en la estantería '" + l.estanteria().getCodigo()
                                + "': disponible " + disponible + ", requerido " + requerido);
            }
        }

        // 4) Descontar stock por línea.
        lineas.forEach(l -> gestorStock.aplicarDelta(lote, l.producto(), l.estanteria(), l.cantidad().negate()));

        // 5) Persistir cabecera + detalle (agregado), con precio congelado y total calculado.
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        Venta venta = Venta.builder()
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .estado(VentaEstado.COMPLETADA)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (LineaVenta l : lineas) {
            BigDecimal subtotal = l.cantidad().multiply(l.precioUnitario()).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);
            venta.agregarDetalle(VentaDetalle.builder()
                    .producto(l.producto())
                    .estanteria(l.estanteria())
                    .cantidad(l.cantidad())
                    .precioUnitario(l.precioUnitario())
                    .subtotal(subtotal)
                    .build());
        }
        venta.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        Venta guardada = ventaRepository.save(venta);

        // 6) Registrar un movimiento SALIDA_VENTA por línea (cantidad negativa = salida).
        String motivo = "Venta #" + guardada.getId();
        lineas.forEach(l -> gestorStock.registrarMovimiento(l.producto(), l.estanteria(), null,
                MovimientoTipo.SALIDA_VENTA, l.cantidad().negate(), usuario, motivo));

        log.info("VENTA #{} registrada: lineas={}, total={}, usuario={}",
                guardada.getId(), lineas.size(), guardada.getTotal(), usuarioId);
        return convertirADTO(guardada);
    }

    @Transactional(readOnly = true)
    public Page<VentaDTO> listar(Long usuarioId, VentaEstado estado,
                                 LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return ventaRepository.buscar(usuarioId, estado, desde, hasta, pageable).map(this::convertirADTO);
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta con ID " + id + " no encontrada"));
        return convertirADTO(venta);
    }

    private LineaVenta validarLinea(CrearVentaDetalleRequest detalle) {
        Producto producto = productoService.obtenerEntidadActiva(detalle.getProductoId());
        if (producto.getTipo() != ProductoTipo.VENTA) {
            throw new OperacionInvalidaException(
                    "Solo se puede vender un producto de tipo VENTA. '" + producto.getCodigo() + "' no lo es");
        }
        if (producto.getPrecioVenta() == null) {
            throw new OperacionInvalidaException(
                    "El producto '" + producto.getCodigo() + "' no tiene precio de venta definido");
        }
        Estanteria estanteria = estanteriaService.obtenerEntidadActiva(detalle.getEstanteriaId());
        return new LineaVenta(producto, estanteria, detalle.getCantidad(), producto.getPrecioVenta());
    }

    private VentaDTO convertirADTO(Venta venta) {
        List<VentaDetalleDTO> detalles = new ArrayList<>();
        for (VentaDetalle d : venta.getDetalles()) {
            detalles.add(VentaDetalleDTO.builder()
                    .id(d.getId())
                    .productoId(d.getProducto().getId())
                    .productoCodigo(d.getProducto().getCodigo())
                    .productoNombre(d.getProducto().getNombre())
                    .estanteriaId(d.getEstanteria().getId())
                    .estanteriaCodigo(d.getEstanteria().getCodigo())
                    .cantidad(d.getCantidad())
                    .precioUnitario(d.getPrecioUnitario())
                    .subtotal(d.getSubtotal())
                    .build());
        }

        return VentaDTO.builder()
                .id(venta.getId())
                .usuarioId(venta.getUsuario().getId())
                .usuarioEmail(venta.getUsuario().getEmail())
                .fecha(venta.getFecha())
                .total(venta.getTotal())
                .estado(venta.getEstado())
                .detalles(detalles)
                .createdAt(venta.getCreatedAt())
                .build();
    }

    /** Tupla interna con una línea de venta ya validada (precio congelado). */
    private record LineaVenta(Producto producto, Estanteria estanteria, BigDecimal cantidad, BigDecimal precioUnitario) {
    }
}
