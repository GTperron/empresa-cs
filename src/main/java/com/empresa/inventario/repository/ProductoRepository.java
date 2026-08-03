package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Producto;
import com.empresa.inventario.enums.ProductoTipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar operaciones CRUD de productos.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    /** Listado paginado con filtros opcionales por tipo y estado activo. */
    @Query("select p from Producto p where (:tipo is null or p.tipo = :tipo) "
            + "and (:activo is null or p.activo = :activo)")
    Page<Producto> buscar(@Param("tipo") ProductoTipo tipo,
                          @Param("activo") Boolean activo,
                          Pageable pageable);
}
