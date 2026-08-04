package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearTransformacionDetalleRequest;
import com.empresa.inventario.dto.CrearTransformacionRequest;
import com.empresa.inventario.dto.TransformacionDTO;
import com.empresa.inventario.dto.TransformacionDetalleDTO;
import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.entity.Stock;
import com.empresa.inventario.entity.Transformacion;
import com.empresa.inventario.entity.TransformacionDetalle;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.enums.MovimientoTipo;
import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.exception.StockInsuficienteException;
import com.empresa.inventario.repository.TransformacionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio que registra transformaciones: consume un insumo (producto ENTRADA) y genera
 * uno o varios productos de venta, actualizando el stock de forma atómica vía GestorStock.
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class TransformacionService {

    private final TransformacionRepository transformacionRepository;
    private final GestorStock gestorStock;
    private final ProductoService productoService;
    private final EstanteriaService estanteriaService;
    private final UsuarioService usuarioService;

    public TransformacionDTO registrar(CrearTransformacionRequest request, Long usuarioId) {
        // 1) Validar insumo (producto ENTRADA activo + estantería origen activa).
        Producto productoEntrada = productoService.obtenerEntidadActiva(request.getProductoEntradaId());
        if (productoEntrada.getTipo() != ProductoTipo.ENTRADA) {
            throw new OperacionInvalidaException(
                    "El producto de entrada debe ser de tipo ENTRADA");
        }
        Estanteria estanteriaOrigen = estanteriaService.obtenerEntidadActiva(request.getEstanteriaOrigenId());
        BigDecimal cantidadConsumida = request.getCantidadConsumida();

        // 2) Validar cada línea generada (producto VENTA activo + estantería destino activa).
        List<LineaGenerada> lineas = request.getDetalles().stream()
                .map(this::validarLineaGenerada)
                .collect(Collectors.toList());

        // 3) Reunir todas las claves de stock y bloquearlas en orden determinístico.
        Set<ClaveStock> claves = new HashSet<>();
        claves.add(new ClaveStock(productoEntrada.getId(), estanteriaOrigen.getId()));
        lineas.forEach(l -> claves.add(new ClaveStock(l.productoVenta().getId(), l.estanteriaDestino().getId())));
        Map<ClaveStock, Stock> lote = gestorStock.bloquearEnOrden(claves);

        // 4) Validar stock suficiente del insumo.
        BigDecimal disponible = gestorStock.disponible(lote, productoEntrada.getId(), estanteriaOrigen.getId());
        if (disponible.compareTo(cantidadConsumida) < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente del insumo '" + productoEntrada.getCodigo()
                            + "': disponible " + disponible + ", requerido " + cantidadConsumida);
        }

        // 5) Aplicar deltas: restar insumo, sumar cada generado.
        gestorStock.aplicarDelta(lote, productoEntrada, estanteriaOrigen, cantidadConsumida.negate());
        lineas.forEach(l -> gestorStock.aplicarDelta(lote, l.productoVenta(), l.estanteriaDestino(), l.cantidadGenerada()));

        // 6) Persistir cabecera + detalle (agregado en cascada).
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        Transformacion transformacion = Transformacion.builder()
                .productoEntrada(productoEntrada)
                .estanteriaOrigen(estanteriaOrigen)
                .cantidadConsumida(cantidadConsumida)
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .observaciones(request.getObservaciones())
                .build();
        lineas.forEach(l -> transformacion.agregarDetalle(TransformacionDetalle.builder()
                .productoVenta(l.productoVenta())
                .estanteriaDestino(l.estanteriaDestino())
                .cantidadGenerada(l.cantidadGenerada())
                .build()));
        Transformacion guardada = transformacionRepository.save(transformacion);

        // 7) Registrar movimientos de trazabilidad: consumo (negativo) + una generación (positiva) por línea.
        String motivoBase = "Transformación #" + guardada.getId();
        gestorStock.registrarMovimiento(productoEntrada, estanteriaOrigen, null,
                MovimientoTipo.TRANSFORMACION, cantidadConsumida.negate(), usuario, motivoBase + " - consumo");
        lineas.forEach(l -> gestorStock.registrarMovimiento(l.productoVenta(), l.estanteriaDestino(), null,
                MovimientoTipo.TRANSFORMACION, l.cantidadGenerada(), usuario, motivoBase + " - generación"));

        log.info("TRANSFORMACION #{} registrada: insumo={}, consumido={}, lineas={}, usuario={}",
                guardada.getId(), productoEntrada.getId(), cantidadConsumida, lineas.size(), usuarioId);
        return convertirADTO(guardada);
    }

    @Transactional(readOnly = true)
    public Page<TransformacionDTO> listar(Long productoEntradaId, Long usuarioId,
                                          LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return transformacionRepository.buscar(productoEntradaId, usuarioId, desde, hasta, pageable)
                .map(this::convertirADTO);
    }

    @Transactional(readOnly = true)
    public TransformacionDTO obtenerPorId(Long id) {
        Transformacion transformacion = transformacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Transformación con ID " + id + " no encontrada"));
        return convertirADTO(transformacion);
    }

    private LineaGenerada validarLineaGenerada(CrearTransformacionDetalleRequest detalle) {
        Producto productoVenta = productoService.obtenerEntidadActiva(detalle.getProductoVentaId());
        if (productoVenta.getTipo() != ProductoTipo.VENTA) {
            throw new OperacionInvalidaException(
                    "El producto generado '" + productoVenta.getCodigo() + "' debe ser de tipo VENTA");
        }
        Estanteria estanteriaDestino = estanteriaService.obtenerEntidadActiva(detalle.getEstanteriaDestinoId());
        return new LineaGenerada(productoVenta, estanteriaDestino, detalle.getCantidadGenerada());
    }

    private TransformacionDTO convertirADTO(Transformacion t) {
        List<TransformacionDetalleDTO> detalles = t.getDetalles().stream()
                .map(d -> TransformacionDetalleDTO.builder()
                        .id(d.getId())
                        .productoVentaId(d.getProductoVenta().getId())
                        .productoVentaCodigo(d.getProductoVenta().getCodigo())
                        .productoVentaNombre(d.getProductoVenta().getNombre())
                        .estanteriaDestinoId(d.getEstanteriaDestino().getId())
                        .estanteriaDestinoCodigo(d.getEstanteriaDestino().getCodigo())
                        .cantidadGenerada(d.getCantidadGenerada())
                        .build())
                .collect(Collectors.toList());

        return TransformacionDTO.builder()
                .id(t.getId())
                .productoEntradaId(t.getProductoEntrada().getId())
                .productoEntradaCodigo(t.getProductoEntrada().getCodigo())
                .productoEntradaNombre(t.getProductoEntrada().getNombre())
                .estanteriaOrigenId(t.getEstanteriaOrigen().getId())
                .estanteriaOrigenCodigo(t.getEstanteriaOrigen().getCodigo())
                .cantidadConsumida(t.getCantidadConsumida())
                .usuarioId(t.getUsuario().getId())
                .usuarioEmail(t.getUsuario().getEmail())
                .fecha(t.getFecha())
                .observaciones(t.getObservaciones())
                .detalles(detalles)
                .createdAt(t.getCreatedAt())
                .build();
    }

    /** Tupla interna con una línea generada ya validada. */
    private record LineaGenerada(Producto productoVenta, Estanteria estanteriaDestino, BigDecimal cantidadGenerada) {
    }
}
