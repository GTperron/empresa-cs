package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Venta;
import com.empresa.inventario.enums.VentaEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

/**
 * Repositorio para consultar el histórico de ventas.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query(value = "select v from Venta v where "
            + "(:usuarioId is null or v.usuario.id = :usuarioId) and "
            + "(:estado is null or v.estado = :estado) and "
            + "(:desde is null or v.fecha >= :desde) and "
            + "(:hasta is null or v.fecha <= :hasta)",
            countQuery = "select count(v) from Venta v where "
            + "(:usuarioId is null or v.usuario.id = :usuarioId) and "
            + "(:estado is null or v.estado = :estado) and "
            + "(:desde is null or v.fecha >= :desde) and "
            + "(:hasta is null or v.fecha <= :hasta)")
    Page<Venta> buscar(@Param("usuarioId") Long usuarioId,
                       @Param("estado") VentaEstado estado,
                       @Param("desde") LocalDateTime desde,
                       @Param("hasta") LocalDateTime hasta,
                       Pageable pageable);
}
