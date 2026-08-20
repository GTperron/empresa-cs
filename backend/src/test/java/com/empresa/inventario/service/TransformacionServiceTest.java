package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearTransformacionDetalleRequest;
import com.empresa.inventario.dto.CrearTransformacionRequest;
import com.empresa.inventario.dto.TransformacionDTO;
import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.entity.Transformacion;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.enums.MovimientoTipo;
import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.enums.UnidadMedida;
import com.empresa.inventario.exception.StockInsuficienteException;
import com.empresa.inventario.repository.TransformacionRepository;
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
 * Tests unitarios de las reglas de negocio de {@link TransformacionService}.
 */
@ExtendWith(MockitoExtension.class)
class TransformacionServiceTest {

    @Mock
    private TransformacionRepository transformacionRepository;

    @Mock
    private GestorStock gestorStock;

    @Mock
    private ProductoService productoService;

    @Mock
    private EstanteriaService estanteriaService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TransformacionService transformacionService;

    private static final Long USUARIO_ID = 9L;

    private Producto producto(Long id, ProductoTipo tipo) {
        return Producto.builder()
                .id(id).codigo("P" + id).nombre("Producto " + id)
                .tipo(tipo).unidadMedida(UnidadMedida.UNIDAD).activo(true)
                .build();
    }

    private Estanteria estanteria(Long id) {
        return Estanteria.builder().id(id).codigo("E-" + id).activo(true).build();
    }

    private Usuario usuario() {
        return Usuario.builder().id(USUARIO_ID).email("op@empresa.com").build();
    }

    @Test
    void transformacion_conStockInsuficienteDelInsumo_lanzaStockInsuficiente() {
        CrearTransformacionRequest request = new CrearTransformacionRequest(
                1L, 10L, new BigDecimal("10"), null,
                List.of(new CrearTransformacionDetalleRequest(2L, 20L, new BigDecimal("3"))));

        when(productoService.obtenerEntidadActiva(1L)).thenReturn(producto(1L, ProductoTipo.ENTRADA));
        when(estanteriaService.obtenerEntidadActiva(10L)).thenReturn(estanteria(10L));
        when(productoService.obtenerEntidadActiva(2L)).thenReturn(producto(2L, ProductoTipo.VENTA));
        when(estanteriaService.obtenerEntidadActiva(20L)).thenReturn(estanteria(20L));
        when(gestorStock.bloquearEnOrden(any())).thenReturn(Collections.emptyMap());
        when(gestorStock.disponible(any(), eq(1L), eq(10L))).thenReturn(new BigDecimal("5"));

        assertThatThrownBy(() -> transformacionService.registrar(request, USUARIO_ID))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("insuficiente");

        verify(gestorStock, never()).aplicarDelta(any(), any(), any(), any());
        verify(transformacionRepository, never()).save(any());
    }

    @Test
    void transformacion_generaVariasLineasDeVenta_correctamente() {
        CrearTransformacionRequest request = new CrearTransformacionRequest(
                1L, 10L, new BigDecimal("10"), "Despiece",
                List.of(
                        new CrearTransformacionDetalleRequest(2L, 20L, new BigDecimal("3")),
                        new CrearTransformacionDetalleRequest(3L, 30L, new BigDecimal("7"))));

        when(productoService.obtenerEntidadActiva(1L)).thenReturn(producto(1L, ProductoTipo.ENTRADA));
        when(estanteriaService.obtenerEntidadActiva(10L)).thenReturn(estanteria(10L));
        when(productoService.obtenerEntidadActiva(2L)).thenReturn(producto(2L, ProductoTipo.VENTA));
        when(estanteriaService.obtenerEntidadActiva(20L)).thenReturn(estanteria(20L));
        when(productoService.obtenerEntidadActiva(3L)).thenReturn(producto(3L, ProductoTipo.VENTA));
        when(estanteriaService.obtenerEntidadActiva(30L)).thenReturn(estanteria(30L));
        when(gestorStock.bloquearEnOrden(any())).thenReturn(Collections.emptyMap());
        when(gestorStock.disponible(any(), eq(1L), eq(10L))).thenReturn(new BigDecimal("100"));
        when(usuarioService.obtenerUsuarioPorId(USUARIO_ID)).thenReturn(usuario());
        when(transformacionRepository.save(any(Transformacion.class))).thenAnswer(inv -> {
            Transformacion t = inv.getArgument(0);
            t.setId(77L);
            return t;
        });

        TransformacionDTO dto = transformacionService.registrar(request, USUARIO_ID);

        // 1 delta de consumo + 2 de generación.
        verify(gestorStock, times(3)).aplicarDelta(any(), any(), any(), any());
        // 1 movimiento de consumo + 2 de generación, todos TRANSFORMACION.
        verify(gestorStock, times(3)).registrarMovimiento(any(), any(), any(),
                eq(MovimientoTipo.TRANSFORMACION), any(), any(), any());
        assertThat(dto.getDetalles()).hasSize(2);
        assertThat(dto.getCantidadConsumida()).isEqualByComparingTo("10");
    }
}
