package com.empresa.inventario.service;

import com.empresa.inventario.dto.AlmacenDTO;
import com.empresa.inventario.dto.CrearAlmacenRequest;
import com.empresa.inventario.dto.EditarAlmacenRequest;
import com.empresa.inventario.entity.Almacen;
import com.empresa.inventario.exception.OperacionInvalidaException;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.exception.RecursoYaExisteException;
import com.empresa.inventario.repository.AlmacenRepository;
import com.empresa.inventario.repository.ZonaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar almacenes (nivel superior de la jerarquía de almacenamiento).
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class AlmacenService {

    private final AlmacenRepository almacenRepository;
    private final ZonaRepository zonaRepository;

    /**
     * Crea un nuevo almacén. El código debe ser único globalmente.
     */
    public AlmacenDTO crear(CrearAlmacenRequest request) {
        if (almacenRepository.existsByCodigo(request.getCodigo())) {
            throw new RecursoYaExisteException(
                    "Ya existe un almacén con el código '" + request.getCodigo() + "'");
        }

        Almacen almacen = Almacen.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .activo(true)
                .build();

        Almacen guardado = almacenRepository.save(almacen);
        log.info("Almacén creado: id={}, codigo={}", guardado.getId(), guardado.getCodigo());
        return convertirADTO(guardado);
    }

    /**
     * Lista almacenes de forma paginada, con filtro opcional por estado activo.
     */
    @Transactional(readOnly = true)
    public Page<AlmacenDTO> listar(Boolean activo, Pageable pageable) {
        Page<Almacen> pagina = (activo == null)
                ? almacenRepository.findAll(pageable)
                : almacenRepository.findByActivo(activo, pageable);
        return pagina.map(this::convertirADTO);
    }

    /**
     * Obtiene el detalle de un almacén por ID.
     */
    @Transactional(readOnly = true)
    public AlmacenDTO obtenerPorId(Long id) {
        return convertirADTO(obtenerEntidad(id));
    }

    /**
     * Obtiene la entidad Almacen o lanza excepción si no existe.
     * Usado internamente y por otros servicios (ej.: ZonaService).
     */
    public Almacen obtenerEntidad(Long id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Almacén con ID " + id + " no encontrado"));
    }

    /**
     * Edita un almacén. Revalida la unicidad del código excluyendo el propio registro.
     */
    public AlmacenDTO editar(Long id, EditarAlmacenRequest request) {
        Almacen almacen = obtenerEntidad(id);

        if (almacenRepository.existsByCodigoAndIdNot(request.getCodigo(), id)) {
            throw new RecursoYaExisteException(
                    "Ya existe otro almacén con el código '" + request.getCodigo() + "'");
        }

        almacen.setCodigo(request.getCodigo());
        almacen.setNombre(request.getNombre());
        almacen.setDireccion(request.getDireccion());

        Almacen actualizado = almacenRepository.save(almacen);
        log.info("Almacén actualizado: id={}", actualizado.getId());
        return convertirADTO(actualizado);
    }

    /**
     * Activa un almacén.
     */
    public AlmacenDTO activar(Long id) {
        Almacen almacen = obtenerEntidad(id);
        almacen.setActivo(true);
        log.info("Almacén activado: id={}", id);
        return convertirADTO(almacenRepository.save(almacen));
    }

    /**
     * Desactiva un almacén (soft delete). No se permite si tiene zonas activas.
     */
    public AlmacenDTO desactivar(Long id) {
        Almacen almacen = obtenerEntidad(id);

        long zonasActivas = zonaRepository.countByAlmacenIdAndActivoTrue(id);
        if (zonasActivas > 0) {
            throw new OperacionInvalidaException(
                    "No se puede desactivar el almacén: tiene " + zonasActivas
                            + " zona(s) activa(s). Desactivá primero las zonas.");
        }

        almacen.setActivo(false);
        log.info("Almacén desactivado: id={}", id);
        return convertirADTO(almacenRepository.save(almacen));
    }

    /**
     * Convierte una entidad Almacen a DTO, incluyendo el conteo de zonas activas.
     */
    private AlmacenDTO convertirADTO(Almacen almacen) {
        return AlmacenDTO.builder()
                .id(almacen.getId())
                .codigo(almacen.getCodigo())
                .nombre(almacen.getNombre())
                .direccion(almacen.getDireccion())
                .activo(almacen.getActivo())
                .cantidadZonasActivas(zonaRepository.countByAlmacenIdAndActivoTrue(almacen.getId()))
                .createdAt(almacen.getCreatedAt())
                .updatedAt(almacen.getUpdatedAt())
                .build();
    }
}
