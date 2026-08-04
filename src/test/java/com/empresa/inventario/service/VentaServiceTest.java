package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearVentaDetalleRequest;
import com.empresa.inventario.dto.CrearVentaRequest;
import com.empresa.inventario.dto.VentaDTO;
import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.entity.Venta;
import com.empresa.inventario.enums.MovimientoTipo;
import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.enums.UnidadMedida;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.StockInsuficienteException;
import com.empresa.inventario.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de las reglas de negocio de {@link VentaService}.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private GestorStock gestorStock;

    @Mock
    private ProductoService productoService;

    @Mock
    private EstanteriaService estanteriaService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private VentaService ventaService;

    private static final Long USUARIO_ID = 9L;

    private Producto producto(Long id, ProductoTipo tipo, BigDecimal precio) {
        return Producto.builder()
                .id(id).codigo("P" + id).nombre("Producto " + id)
                .tipo(tipo).unidadMedida(UnidadMedida.UNIDAD).precioVenta(precio).activo(true)
                .build();
    }

    private Estanteria estanteria(Long id) {
        return Estanteria.builder().id(id).codigo("E-" + id).activo(true).build();
    }

    private Usuario usuario() {
        return Usuario.builder().id(USUARIO_ID).email("op@empresa.com").build();
    }

    @Test
    void venta_descuentaStockYCalculaTotal_correctamente() {
        CrearVentaRequest request = new CrearVentaRequest(
                List.of(new CrearVentaDetalleRequest(2L, 20L, new BigDecimal("2"))));

        Producto p = producto(2L, ProductoTipo.VENTA, new BigDecimal("100"));
        Estanteria e = estanteria(20L);
        when(productoService.obtenerEntidadActiva(2L)).thenReturn(p);
        when(estanteriaService.obtenerEntidadActiva(20L)).thenReturn(e);
        when(gestorStock.bloquearEnOrden(any())).thenReturn(Collections.emptyMap());
        when(gestorStock.disponible(any(), eq(2L), eq(20L))).thenReturn(new BigDecimal("50"));
        when(usuarioService.obtenerUsuarioPorId(USUARIO_ID)).thenReturn(usuario());
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(77L);
            return v;
        });

        VentaDTO dto = ventaService.registrar(request, USUARIO_ID);

        verify(gestorStock).aplicarDelta(any(), eq(p), eq(e), eq(new BigDecimal("-2")));
        verify(gestorStock, times(1)).registrarMovimiento(any(), any(), any(),
                eq(MovimientoTipo.SALIDA_VENTA), any(), any(), any());
        assertThat(dto.getDetalles()).hasSize(1);
        assertThat(dto.getTotal()).isEqualByComparingTo("200.00");
        assertThat(dto.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo("100");
        assertThat(dto.getDetalles().get(0).getSubtotal()).isEqualByComparingTo("200.00");
    }

    @Test
    void venta_deProductoTipoEntrada_lanzaOperacionInvalida() {
        CrearVentaRequest request = new CrearVentaRequest(
                List.of(new CrearVentaDetalleRequest(2L, 20L, new BigDecimal("2"))));
        when(productoService.obtenerEntidadActiva(2L)).thenReturn(producto(2L, ProductoTipo.ENTRADA, null));

        assertThatThrownBy(() -> ventaService.registrar(request, USUARIO_ID))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("VENTA");

        verify(ventaRepository, never()).save(any());
        verify(gestorStock, never()).aplicarDelta(any(), any(), any(), any());
    }

    @Test
    void venta_conStockInsuficiente_lanzaStockInsuficiente() {
        CrearVentaRequest request = new CrearVentaRequest(
                List.of(new CrearVentaDetalleRequest(2L, 20L, new BigDecimal("999"))));

        when(productoService.obtenerEntidadActiva(2L)).thenReturn(producto(2L, ProductoTipo.VENTA, new BigDecimal("100")));
        when(estanteriaService.obtenerEntidadActiva(20L)).thenReturn(estanteria(20L));
        when(gestorStock.bloquearEnOrden(any())).thenReturn(Collections.emptyMap());
        when(gestorStock.disponible(any(), eq(2L), eq(20L))).thenReturn(new BigDecimal("5"));

        assertThatThrownBy(() -> ventaService.registrar(request, USUARIO_ID))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("insuficiente");

        verify(gestorStock, never()).aplicarDelta(any(), any(), any(), any());
        verify(ventaRepository, never()).save(any());
    }
}
