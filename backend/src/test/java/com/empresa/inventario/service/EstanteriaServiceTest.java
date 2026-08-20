package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearEstanteriaRequest;
import com.empresa.inventario.entity.Almacen;
import com.empresa.inventario.entity.Zona;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoYaExisteException;
import com.empresa.inventario.repository.EstanteriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de las reglas de negocio de {@link EstanteriaService}.
 */
@ExtendWith(MockitoExtension.class)
class EstanteriaServiceTest {

    @Mock
    private EstanteriaRepository estanteriaRepository;

    @Mock
    private ZonaService zonaService;

    @InjectMocks
    private EstanteriaService estanteriaService;

    private Zona zonaActiva(boolean activa) {
        Almacen almacen = Almacen.builder().id(1L).codigo("A1").activo(true).build();
        return Zona.builder().id(5L).almacen(almacen).codigo("A").activo(activa).build();
    }

    @Test
    void crear_conCodigoDuplicadoEnZona_lanzaRecursoYaExiste() {
        CrearEstanteriaRequest request = new CrearEstanteriaRequest("E1", "Estante 1", "desc", 100);
        when(zonaService.obtenerEntidad(5L)).thenReturn(zonaActiva(true));
        when(estanteriaRepository.existsByZonaIdAndCodigo(5L, "E1")).thenReturn(true);

        assertThatThrownBy(() -> estanteriaService.crear(5L, request))
                .isInstanceOf(RecursoYaExisteException.class);

        verify(estanteriaRepository, never()).save(any());
    }

    @Test
    void crear_enZonaInactiva_lanzaOperacionInvalida() {
        CrearEstanteriaRequest request = new CrearEstanteriaRequest("E1", "Estante 1", "desc", 100);
        when(zonaService.obtenerEntidad(5L)).thenReturn(zonaActiva(false));

        assertThatThrownBy(() -> estanteriaService.crear(5L, request))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("inactiva");

        verify(estanteriaRepository, never()).existsByZonaIdAndCodigo(anyLong(), anyString());
        verify(estanteriaRepository, never()).save(any());
    }
}
