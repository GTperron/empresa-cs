package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Transformacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

/**
 * Repositorio para consultar el histórico de transformaciones.
 */
@Repository
public interface TransformacionRepository extends JpaRepository<Transformacion, Long> {

    @Query(value = "select t from Transformacion t where "
            + "(:productoEntradaId is null or t.productoEntrada.id = :productoEntradaId) and "
            + "(:usuarioId is null or t.usuario.id = :usuarioId) and "
            + "(:desde is null or t.fecha >= :desde) and "
            + "(:hasta is null or t.fecha <= :hasta)",
            countQuery = "select count(t) from Transformacion t where "
            + "(:productoEntradaId is null or t.productoEntrada.id = :productoEntradaId) and "
            + "(:usuarioId is null or t.usuario.id = :usuarioId) and "
            + "(:desde is null or t.fecha >= :desde) and "
            + "(:hasta is null or t.fecha <= :hasta)")
    Page<Transformacion> buscar(@Param("productoEntradaId") Long productoEntradaId,
                                @Param("usuarioId") Long usuarioId,
                                @Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta,
                                Pageable pageable);
}
