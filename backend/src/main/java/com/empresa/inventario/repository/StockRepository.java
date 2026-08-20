package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar el stock por producto y estantería.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Obtiene la fila de stock (producto + estantería) tomando un lock de escritura
     * pesimista (SELECT ... FOR UPDATE). Debe invocarse dentro de una transacción.
     * Serializa los movimientos concurrentes sobre la misma fila.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.producto.id = :productoId and s.estanteria.id = :estanteriaId")
    Optional<Stock> lockByProductoAndEstanteria(@Param("productoId") Long productoId,
                                                @Param("estanteriaId") Long estanteriaId);

    /** Búsqueda sin lock por producto + estantería. */
    Optional<Stock> findByProductoIdAndEstanteriaId(Long productoId, Long estanteriaId);

    /** Todas las ubicaciones (estanterías) donde hay una fila de stock de un producto. */
    List<Stock> findByProductoId(Long productoId);

    /**
     * Listado paginado de stock con filtros opcionales; el filtro por almacén
     * hace join hacia arriba en la jerarquía (estantería -> zona -> almacén).
     */
    @Query(value = "select s from Stock s where "
            + "(:productoId is null or s.producto.id = :productoId) and "
            + "(:estanteriaId is null or s.estanteria.id = :estanteriaId) and "
            + "(:almacenId is null or s.estanteria.zona.almacen.id = :almacenId)",
            countQuery = "select count(s) from Stock s where "
            + "(:productoId is null or s.producto.id = :productoId) and "
            + "(:estanteriaId is null or s.estanteria.id = :estanteriaId) and "
            + "(:almacenId is null or s.estanteria.zona.almacen.id = :almacenId)")
    Page<Stock> buscar(@Param("productoId") Long productoId,
                       @Param("estanteriaId") Long estanteriaId,
                       @Param("almacenId") Long almacenId,
                       Pageable pageable);
}
