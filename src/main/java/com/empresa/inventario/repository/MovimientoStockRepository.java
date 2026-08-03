package com.empresa.inventario.repository;

import com.empresa.inventario.entity.MovimientoStock;
import com.empresa.inventario.enums.MovimientoTipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

/**
 * Repositorio para consultar el histórico de movimientos de stock.
 */
@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    /**
     * Historial paginado con filtros opcionales. El filtro por estantería considera
     * tanto la de origen como la de destino (left join para no excluir movimientos sin destino).
     */
    @Query(value = "select m from MovimientoStock m left join m.estanteriaDestino ed where "
            + "(:productoId is null or m.producto.id = :productoId) and "
            + "(:estanteriaId is null or m.estanteria.id = :estanteriaId or ed.id = :estanteriaId) and "
            + "(:tipo is null or m.tipo = :tipo) and "
            + "(:usuarioId is null or m.usuario.id = :usuarioId) and "
            + "(:desde is null or m.fecha >= :desde) and "
            + "(:hasta is null or m.fecha <= :hasta)",
            countQuery = "select count(m) from MovimientoStock m left join m.estanteriaDestino ed where "
            + "(:productoId is null or m.producto.id = :productoId) and "
            + "(:estanteriaId is null or m.estanteria.id = :estanteriaId or ed.id = :estanteriaId) and "
            + "(:tipo is null or m.tipo = :tipo) and "
            + "(:usuarioId is null or m.usuario.id = :usuarioId) and "
            + "(:desde is null or m.fecha >= :desde) and "
            + "(:hasta is null or m.fecha <= :hasta)")
    Page<MovimientoStock> buscar(@Param("productoId") Long productoId,
                                 @Param("estanteriaId") Long estanteriaId,
                                 @Param("tipo") MovimientoTipo tipo,
                                 @Param("usuarioId") Long usuarioId,
                                 @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta,
                                 Pageable pageable);
}
