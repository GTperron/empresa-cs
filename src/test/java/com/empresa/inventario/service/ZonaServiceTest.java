package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearZonaRequest;
import com.empresa.inventario.entity.Almacen;
import com.empresa.inventario.entity.Zona;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoYaExisteException;
import com.empresa.inventario.repository.EstanteriaRepository;
import com.empresa.inventario.repository.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de las reglas de negocio de {@link ZonaService}.
 */
@ExtendWith(MockitoExtension.class)
class ZonaServiceTest {

    @Mock
    private ZonaRepository zonaRepository;

    @Mock
    private EstanteriaRepository estanteriaRepository;

    @Mock
    private AlmacenService almacenService;

    @InjectMocks
    private ZonaService zonaService;

    @Test
    void crear_conCodigoDuplicadoEnAlmacen_lanzaRecursoYaExiste() {
        Almacen almacen = Almacen.builder().id(1L).codigo("A1").activo(true).build();
        CrearZonaRequest request = new CrearZonaRequest("A", "Zona A", "desc");
        when(almacenService.obtenerEntidad(1L)).thenReturn(almacen);
        when(zonaRepository.existsByAlmacenIdAndCodigo(1L, "A")).thenReturn(true);

        assertThatThrownBy(() -> zonaService.crear(1L, request))
                .isInstanceOf(RecursoYaExisteException.class);

        verify(zonaRepository, never()).save(any());
    }

    @Test
    void crear_enAlmacenInactivo_lanzaOperacionInvalida() {
        Almacen almacen = Almacen.builder().id(1L).codigo("A1").activo(false).build();
        CrearZonaRequest request = new CrearZonaRequest("A", "Zona A", "desc");
        when(almacenService.obtenerEntidad(1L)).thenReturn(almacen);

        assertThatThrownBy(() -> zonaService.crear(1L, request))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("inactivo");

        // No debe llegar a validar unicidad ni a persistir.
        verify(zonaRepository, never()).existsByAlmacenIdAndCodigo(anyLong(), anyString());
        verify(zonaRepository, never()).save(any());
    }

    @Test
    void desactivar_conEstanteriasActivas_lanzaOperacionInvalida() {
        Almacen almacen = Almacen.builder().id(1L).codigo("A1").activo(true).build();
        Zona zona = Zona.builder().id(5L).almacen(almacen).codigo("A").activo(true).build();
        when(zonaRepository.findById(5L)).thenReturn(Optional.of(zona));
        when(estanteriaRepository.countByZonaIdAndActivoTrue(5L)).thenReturn(2L);

        assertThatThrownBy(() -> zonaService.desactivar(5L))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("estantería");

        verify(zonaRepository, never()).save(any());
    }
}
