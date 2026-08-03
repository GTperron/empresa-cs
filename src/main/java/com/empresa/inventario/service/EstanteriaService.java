package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearEstanteriaRequest;
import com.empresa.inventario.dto.EditarEstanteriaRequest;
import com.empresa.inventario.dto.EstanteriaDTO;
import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.Zona;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.exception.RecursoYaExisteException;
import com.empresa.inventario.repository.EstanteriaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar estanterías (nivel inferior de la jerarquía de almacenamiento).
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class EstanteriaService {

    private final EstanteriaRepository estanteriaRepository;
    private final ZonaService zonaService;

    /**
     * Crea una estantería dentro de una zona. El código debe ser único dentro de esa zona.
     */
    public EstanteriaDTO crear(Long zonaId, CrearEstanteriaRequest request) {
        Zona zona = zonaService.obtenerEntidad(zonaId);

        // Evita crear hijos activos bajo un padre inactivo.
        if (!zona.getActivo()) {
            throw new OperacionInvalidaException(
                    "No se puede crear una estantería en una zona inactiva");
        }

        if (estanteriaRepository.existsByZonaIdAndCodigo(zonaId, request.getCodigo())) {
            throw new RecursoYaExisteException(
                    "Ya existe una estantería con el código '" + request.getCodigo() + "' en esta zona");
        }

        Estanteria estanteria = Estanteria.builder()
                .zona(zona)
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .capacidadMaxima(request.getCapacidadMaxima())
                .activo(true)
                .build();

        Estanteria guardada = estanteriaRepository.save(estanteria);
        log.info("Estantería creada: id={}, codigo={}, zonaId={}",
                guardada.getId(), guardada.getCodigo(), zonaId);
        return convertirADTO(guardada);
    }

    /**
     * Lista las estanterías de una zona, con filtro opcional por estado activo.
     */
    @Transactional(readOnly = true)
    public List<EstanteriaDTO> listarPorZona(Long zonaId, Boolean activo) {
        // Valida existencia de la zona (lanza 404 si no existe).
        zonaService.obtenerEntidad(zonaId);

        List<Estanteria> estanterias = (activo == null)
                ? estanteriaRepository.findByZonaId(zonaId)
                : estanteriaRepository.findByZonaIdAndActivo(zonaId, activo);

        return estanterias.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle de una estantería por ID.
     */
    @Transactional(readOnly = true)
    public EstanteriaDTO obtenerPorId(Long id) {
        return convertirADTO(obtenerEntidad(id));
    }

    /**
     * Obtiene la entidad Estanteria o lanza excepción si no existe.
     */
    public Estanteria obtenerEntidad(Long id) {
        return estanteriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estantería con ID " + id + " no encontrada"));
    }

    /**
     * Edita una estantería. Revalida la unicidad del código dentro de la misma zona.
     */
    public EstanteriaDTO editar(Long id, EditarEstanteriaRequest request) {
        Estanteria estanteria = obtenerEntidad(id);
        Long zonaId = estanteria.getZona().getId();

        if (estanteriaRepository.existsByZonaIdAndCodigoAndIdNot(zonaId, request.getCodigo(), id)) {
            throw new RecursoYaExisteException(
                    "Ya existe otra estantería con el código '" + request.getCodigo() + "' en esta zona");
        }

        estanteria.setCodigo(request.getCodigo());
        estanteria.setNombre(request.getNombre());
        estanteria.setDescripcion(request.getDescripcion());
        estanteria.setCapacidadMaxima(request.getCapacidadMaxima());

        Estanteria actualizada = estanteriaRepository.save(estanteria);
        log.info("Estantería actualizada: id={}", actualizada.getId());
        return convertirADTO(actualizada);
    }

    /**
     * Activa una estantería. No se permite si su zona está inactiva.
     */
    public EstanteriaDTO activar(Long id) {
        Estanteria estanteria = obtenerEntidad(id);

        if (!estanteria.getZona().getActivo()) {
            throw new OperacionInvalidaException(
                    "No se puede activar la estantería: su zona está inactiva");
        }

        estanteria.setActivo(true);
        log.info("Estantería activada: id={}", id);
        return convertirADTO(estanteriaRepository.save(estanteria));
    }

    /**
     * Desactiva una estantería (soft delete).
     */
    public EstanteriaDTO desactivar(Long id) {
        Estanteria estanteria = obtenerEntidad(id);

        // TODO (Módulo 3): impedir la desactivación si la estantería tiene stock asociado.

        estanteria.setActivo(false);
        log.info("Estantería desactivada: id={}", id);
        return convertirADTO(estanteriaRepository.save(estanteria));
    }

    /**
     * Convierte una entidad Estanteria a DTO.
     */
    private EstanteriaDTO convertirADTO(Estanteria estanteria) {
        return EstanteriaDTO.builder()
                .id(estanteria.getId())
                .zonaId(estanteria.getZona().getId())
                .codigo(estanteria.getCodigo())
                .nombre(estanteria.getNombre())
                .descripcion(estanteria.getDescripcion())
                .capacidadMaxima(estanteria.getCapacidadMaxima())
                .activo(estanteria.getActivo())
                .createdAt(estanteria.getCreatedAt())
                .updatedAt(estanteria.getUpdatedAt())
                .build();
    }
}
