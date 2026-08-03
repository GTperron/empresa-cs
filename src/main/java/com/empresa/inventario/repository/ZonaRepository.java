package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para gestionar operaciones CRUD de zonas.
 */
@Repository
public interface ZonaRepository extends JpaRepository<Zona, Long> {

    /** Unicidad del código DENTRO del almacén (al crear). */
    boolean existsByAlmacenIdAndCodigo(Long almacenId, String codigo);

    /** Unicidad del código DENTRO del almacén (al editar), excluyendo el propio registro. */
    boolean existsByAlmacenIdAndCodigoAndIdNot(Long almacenId, String codigo, Long id);

    /** Todas las zonas de un almacén. */
    List<Zona> findByAlmacenId(Long almacenId);

    /** Zonas de un almacén filtradas por estado. */
    List<Zona> findByAlmacenIdAndActivo(Long almacenId, Boolean activo);

    /** Cantidad de zonas activas de un almacén (usado para bloquear su desactivación). */
    long countByAlmacenIdAndActivoTrue(Long almacenId);
}
