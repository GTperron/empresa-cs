package com.empresa.inventario.service;

import com.empresa.inventario.dto.AjusteRequest;
import com.empresa.inventario.dto.EntradaRequest;
import com.empresa.inventario.dto.TrasladoRequest;
import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.MovimientoStock;
import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.enums.MovimientoTipo;
import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.enums.UnidadMedida;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.StockInsuficienteException;
import com.empresa.inventario.repository.MovimientoStockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de las reglas de negocio de {@link MovimientoStockService}.
 * La mutación de stock se aísla mockeando {@link GestorStock}.
 */
@ExtendWith(MockitoExtension.class)
class MovimientoStockServiceTest {

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @Mock
    private GestorStock gestorStock;

    @Mock
    private ProductoService productoService;

    @Mock
    private EstanteriaService estanteriaService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private MovimientoStockService movimientoStockService;

    private static final Long USUARIO_ID = 9L;

    private Producto producto(ProductoTipo tipo) {
        return Producto.builder()
                .id(1L).codigo("P1").nombre("Producto 1")
                .tipo(tipo).unidadMedida(UnidadMedida.UNIDAD).activo(true)
                .build();
    }

    private Estanteria estanteria(Long id) {
        return Estanteria.builder().id(id).codigo("E-" + id).activo(true).build();
    }

    private Usuario usuario() {
        return Usuario.builder().id(USUARIO_ID).email("op@empresa.com").build();
    }

    private MovimientoStock movimiento(Producto p, Estanteria e, MovimientoTipo tipo, BigDecimal cantidad) {
        return MovimientoStock.builder()
                .id(500L).producto(p).estanteria(e).tipo(tipo).cantidad(cantidad)
                .usuario(usuario()).fecha(LocalDateTime.now()).build();
    }

    @Test
    void entrada_delegaEnGestorStock_yRegistraMovimiento() {
        EntradaRequest request = new EntradaRequest(1L, 2L, new BigDecimal("10"), null);
        Producto p = producto(ProductoTipo.ENTRADA);
        Estanteria e = estanteria(2L);
        when(productoService.obtenerEntidadActiva(1L)).thenReturn(p);
        when(estanteriaService.obtenerEntidadActiva(2L)).thenReturn(e);
        when(gestorStock.bloquearEnOrden(any())).thenReturn(Collections.emptyMap());
        when(usuarioService.obtenerUsuarioPorId(USUARIO_ID)).thenReturn(usuario());
        when(gestorStock.registrarMovimiento(any(), any(), any(), eq(MovimientoTipo.ENTRADA), any(), any(), any()))
                .thenReturn(movimiento(p, e, MovimientoTipo.ENTRADA, new BigDecimal("10")));

        movimientoStockService.registrarEntrada(request, USUARIO_ID);

        verify(gestorStock).aplicarDelta(any(), eq(p), eq(e), eq(new BigDecimal("10")));
        verify(gestorStock).registrarMovimiento(any(), any(), any(), eq(MovimientoTipo.ENTRADA), any(), any(), any());
    }

    @Test
    void entrada_sobreProductoVenta_lanzaOperacionInvalida() {
        EntradaRequest request = new EntradaRequest(1L, 2L, new BigDecimal("10"), null);
        when(productoService.obtenerEntidadActiva(1L)).thenReturn(producto(ProductoTipo.VENTA));

        assertThatThrownBy(() -> movimientoStockService.registrarEntrada(request, USUARIO_ID))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("ENTRADA");

        verify(gestorStock, never()).registrarMovimiento(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void traslado_conOrigenIgualDestino_lanzaOperacionInvalida() {
        TrasladoRequest request = new TrasladoRequest(1L, 2L, 2L, new BigDecimal("5"), null);

        assertThatThrownBy(() -> movimientoStockService.registrarTraslado(request, USUARIO_ID))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("misma estantería");

        verify(gestorStock, never()).registrarMovimiento(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void traslado_conStockInsuficienteEnOrigen_lanzaStockInsuficiente() {
        TrasladoRequest request = new TrasladoRequest(1L, 2L, 3L, new BigDecimal("10"), null);
        Producto p = producto(ProductoTipo.ENTRADA);
        when(productoService.obtenerEntidadActiva(1L)).thenReturn(p);
        when(estanteriaService.obtenerEntidadActiva(2L)).thenReturn(estanteria(2L));
        when(estanteriaService.obtenerEntidadActiva(3L)).thenReturn(estanteria(3L));
        when(gestorStock.bloquearEnOrden(any())).thenReturn(Collections.emptyMap());
        when(gestorStock.disponible(any(), anyLong(), anyLong())).thenReturn(new BigDecimal("5"));

        assertThatThrownBy(() -> movimientoStockService.registrarTraslado(request, USUARIO_ID))
                .isInstanceOf(StockInsuficienteException.class);

        verify(gestorStock, never()).aplicarDelta(any(), any(), any(), any());
        verify(gestorStock, never()).registrarMovimiento(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void ajuste_queDejariaStockNegativo_lanzaStockInsuficiente() {
        AjusteRequest request = new AjusteRequest(1L, 2L, new BigDecimal("-10"), "Merma");
        Producto p = producto(ProductoTipo.ENTRADA);
        when(productoService.obtenerEntidadActiva(1L)).thenReturn(p);
        when(estanteriaService.obtenerEntidadActiva(2L)).thenReturn(estanteria(2L));
        when(gestorStock.bloquearEnOrden(any())).thenReturn(Collections.emptyMap());
        when(gestorStock.aplicarDelta(any(), any(), any(), any()))
                .thenThrow(new StockInsuficienteException("El movimiento dejaría el stock en negativo"));

        assertThatThrownBy(() -> movimientoStockService.registrarAjuste(request, USUARIO_ID))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("negativo");

        verify(gestorStock, never()).registrarMovimiento(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void ajuste_sinMotivo_lanzaOperacionInvalida() {
        AjusteRequest request = new AjusteRequest(1L, 2L, new BigDecimal("5"), null);

        assertThatThrownBy(() -> movimientoStockService.registrarAjuste(request, USUARIO_ID))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("motivo");

        verify(productoService, never()).obtenerEntidadActiva(anyLong());
        verify(gestorStock, never()).registrarMovimiento(any(), any(), any(), any(), any(), any(), any());
    }
}
