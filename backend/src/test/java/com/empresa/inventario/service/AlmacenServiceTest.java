package com.empresa.inventario.service;

import com.empresa.inventario.dto.AlmacenDTO;
import com.empresa.inventario.dto.CrearAlmacenRequest;
import com.empresa.inventario.entity.Almacen;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoYaExisteException;
import com.empresa.inventario.repository.AlmacenRepository;
import com.empresa.inventario.repository.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de las reglas de negocio de {@link AlmacenService}.
 */
@ExtendWith(MockitoExtension.class)
class AlmacenServiceTest {

    @Mock
    private AlmacenRepository almacenRepository;

    @Mock
    private ZonaRepository zonaRepository;

    @InjectMocks
    private AlmacenService almacenService;

    @Test
    void crear_conCodigoDuplicado_lanzaRecursoYaExiste() {
        CrearAlmacenRequest request = new CrearAlmacenRequest("A1", "Central", "Calle 1");
        when(almacenRepository.existsByCodigo("A1")).thenReturn(true);

        assertThatThrownBy(() -> almacenService.crear(request))
                .isInstanceOf(RecursoYaExisteException.class);

        verify(almacenRepository, never()).save(any());
    }

    @Test
    void crear_conCodigoNuevo_persisteYDevuelveDTO() {
        CrearAlmacenRequest request = new CrearAlmacenRequest("A1", "Central", "Calle 1");
        when(almacenRepository.existsByCodigo("A1")).thenReturn(false);
        when(almacenRepository.save(any(Almacen.class))).thenAnswer(inv -> {
            Almacen a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });
        when(zonaRepository.countByAlmacenIdAndActivoTrue(10L)).thenReturn(0L);

        AlmacenDTO dto = almacenService.crear(request);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getCodigo()).isEqualTo("A1");
        assertThat(dto.getActivo()).isTrue();
        assertThat(dto.getCantidadZonasActivas()).isZero();
    }

    @Test
    void desactivar_conZonasActivas_lanzaOperacionInvalida() {
        Almacen almacen = Almacen.builder().id(1L).codigo("A1").nombre("Central").activo(true).build();
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen));
        when(zonaRepository.countByAlmacenIdAndActivoTrue(1L)).thenReturn(3L);

        assertThatThrownBy(() -> almacenService.desactivar(1L))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("zona");

        verify(almacenRepository, never()).save(any());
    }

    @Test
    void desactivar_sinZonasActivas_marcaInactivo() {
        Almacen almacen = Almacen.builder().id(1L).codigo("A1").nombre("Central").activo(true).build();
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen));
        when(zonaRepository.countByAlmacenIdAndActivoTrue(1L)).thenReturn(0L);
        when(almacenRepository.save(any(Almacen.class))).thenAnswer(inv -> inv.getArgument(0));

        AlmacenDTO dto = almacenService.desactivar(1L);

        assertThat(dto.getActivo()).isFalse();
        verify(almacenRepository).save(almacen);
    }
}
