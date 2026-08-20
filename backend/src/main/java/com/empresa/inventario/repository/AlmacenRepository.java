package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Almacen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar operaciones CRUD de almacenes.
 */
@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {

    /** Verifica unicidad global del código al crear. */
    boolean existsByCodigo(String codigo);

    /** Verifica unicidad global del código al editar, excluyendo el propio registro. */
    boolean existsByCodigoAndIdNot(String codigo, Long id);

    /** Listado paginado filtrando por estado activo/inactivo. */
    Page<Almacen> findByActivo(Boolean activo, Pageable pageable);
}
