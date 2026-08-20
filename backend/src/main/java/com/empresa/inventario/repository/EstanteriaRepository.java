package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Estanteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para gestionar operaciones CRUD de estanterías.
 */
@Repository
public interface EstanteriaRepository extends JpaRepository<Estanteria, Long> {

    /** Unicidad del código DENTRO de la zona (al crear). */
    boolean existsByZonaIdAndCodigo(Long zonaId, String codigo);

    /** Unicidad del código DENTRO de la zona (al editar), excluyendo el propio registro. */
    boolean existsByZonaIdAndCodigoAndIdNot(Long zonaId, String codigo, Long id);

    /** Todas las estanterías de una zona. */
    List<Estanteria> findByZonaId(Long zonaId);

    /** Estanterías de una zona filtradas por estado. */
    List<Estanteria> findByZonaIdAndActivo(Long zonaId, Boolean activo);

    /** Cantidad de estanterías activas de una zona (usado para bloquear su desactivación). */
    long countByZonaIdAndActivoTrue(Long zonaId);
}
