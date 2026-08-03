package com.empresa.inventario.service;

import com.empresa.inventario.dto.CrearZonaRequest;
import com.empresa.inventario.dto.EditarZonaRequest;
import com.empresa.inventario.dto.ZonaDTO;
import com.empresa.inventario.entity.Almacen;
import com.empresa.inventario.entity.Zona;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.exception.RecursoYaExisteException;
import com.empresa.inventario.repository.EstanteriaRepository;
import com.empresa.inventario.repository.ZonaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar zonas (nivel intermedio de la jerarquía de almacenamiento).
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class ZonaService {

    private final ZonaRepository zonaRepository;
    private final EstanteriaRepository estanteriaRepository;
    private final AlmacenService almacenService;

    /**
     * Crea una zona dentro de un almacén. El código debe ser único dentro de ese almacén.
     */
    public ZonaDTO crear(Long almacenId, CrearZonaRequest request) {
        Almacen almacen = almacenService.obtenerEntidad(almacenId);

        // Evita crear hijos activos bajo un padre inactivo.
        if (!almacen.getActivo()) {
            throw new OperacionInvalidaException(
                    "No se puede crear una zona en un almacén inactivo");
        }

        if (zonaRepository.existsByAlmacenIdAndCodigo(almacenId, request.getCodigo())) {
            throw new RecursoYaExisteException(
                    "Ya existe una zona con el código '" + request.getCodigo() + "' en este almacén");
        }

        Zona zona = Zona.builder()
                .almacen(almacen)
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(true)
                .build();

        Zona guardada = zonaRepository.save(zona);
        log.info("Zona creada: id={}, codigo={}, almacenId={}",
                guardada.getId(), guardada.getCodigo(), almacenId);
        return convertirADTO(guardada);
    }

    /**
     * Lista las zonas de un almacén, con filtro opcional por estado activo.
     */
    @Transactional(readOnly = true)
    public List<ZonaDTO> listarPorAlmacen(Long almacenId, Boolean activo) {
        // Valida existencia del almacén (lanza 404 si no existe).
        almacenService.obtenerEntidad(almacenId);

        List<Zona> zonas = (activo == null)
                ? zonaRepository.findByAlmacenId(almacenId)
                : zonaRepository.findByAlmacenIdAndActivo(almacenId, activo);

        return zonas.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle de una zona por ID.
     */
    @Transactional(readOnly = true)
    public ZonaDTO obtenerPorId(Long id) {
        return convertirADTO(obtenerEntidad(id));
    }

    /**
     * Obtiene la entidad Zona o lanza excepción si no existe.
     * Usado internamente y por otros servicios (ej.: EstanteriaService).
     */
    public Zona obtenerEntidad(Long id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Zona con ID " + id + " no encontrada"));
    }

    /**
     * Edita una zona. Revalida la unicidad del código dentro del mismo almacén.
     */
    public ZonaDTO editar(Long id, EditarZonaRequest request) {
        Zona zona = obtenerEntidad(id);
        Long almacenId = zona.getAlmacen().getId();

        if (zonaRepository.existsByAlmacenIdAndCodigoAndIdNot(almacenId, request.getCodigo(), id)) {
            throw new RecursoYaExisteException(
                    "Ya existe otra zona con el código '" + request.getCodigo() + "' en este almacén");
        }

        zona.setCodigo(request.getCodigo());
        zona.setNombre(request.getNombre());
        zona.setDescripcion(request.getDescripcion());

        Zona actualizada = zonaRepository.save(zona);
        log.info("Zona actualizada: id={}", actualizada.getId());
        return convertirADTO(actualizada);
    }

    /**
     * Activa una zona. No se permite si su almacén está inactivo.
     */
    public ZonaDTO activar(Long id) {
        Zona zona = obtenerEntidad(id);

        if (!zona.getAlmacen().getActivo()) {
            throw new OperacionInvalidaException(
                    "No se puede activar la zona: su almacén está inactivo");
        }

        zona.setActivo(true);
        log.info("Zona activada: id={}", id);
        return convertirADTO(zonaRepository.save(zona));
    }

    /**
     * Desactiva una zona (soft delete). No se permite si tiene estanterías activas.
     */
    public ZonaDTO desactivar(Long id) {
        Zona zona = obtenerEntidad(id);

        long estanteriasActivas = estanteriaRepository.countByZonaIdAndActivoTrue(id);
        if (estanteriasActivas > 0) {
            throw new OperacionInvalidaException(
                    "No se puede desactivar la zona: tiene " + estanteriasActivas
                            + " estantería(s) activa(s). Desactivá primero las estanterías.");
        }

        // TODO (Módulo 3): impedir la desactivación si la zona tiene stock asociado.

        zona.setActivo(false);
        log.info("Zona desactivada: id={}", id);
        return convertirADTO(zonaRepository.save(zona));
    }

    /**
     * Convierte una entidad Zona a DTO, incluyendo el conteo de estanterías activas.
     */
    private ZonaDTO convertirADTO(Zona zona) {
        return ZonaDTO.builder()
                .id(zona.getId())
                .almacenId(zona.getAlmacen().getId())
                .codigo(zona.getCodigo())
                .nombre(zona.getNombre())
                .descripcion(zona.getDescripcion())
                .activo(zona.getActivo())
                .cantidadEstanteriasActivas(estanteriaRepository.countByZonaIdAndActivoTrue(zona.getId()))
                .createdAt(zona.getCreatedAt())
                .updatedAt(zona.getUpdatedAt())
                .build();
    }
}
