package com.empresa.inventario.repository.spec;

import com.empresa.inventario.entity.Estanteria;
import com.empresa.inventario.entity.MovimientoStock;
import com.empresa.inventario.enums.MovimientoTipo;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Predicados opcionales del historial de movimientos.
 * Solo se agrega al WHERE el filtro que viene con valor (evita el patrón JPQL
 * {@code :param is null or …} que PostgreSQL no tipa bien con timestamps).
 */
public final class MovimientoStockSpecs {

    private MovimientoStockSpecs() {
    }

    public static Specification<MovimientoStock> conFiltros(Long productoId,
                                                            Long estanteriaId,
                                                            MovimientoTipo tipo,
                                                            Long usuarioId,
                                                            LocalDateTime desde,
                                                            LocalDateTime hasta) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productoId != null) {
                predicates.add(cb.equal(root.get("producto").get("id"), productoId));
            }
            if (estanteriaId != null) {
                // Left join: un movimiento sin destino (ENTRADA/AJUSTE) sigue matcheando por origen.
                Join<MovimientoStock, Estanteria> destino = root.join("estanteriaDestino", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.equal(root.get("estanteria").get("id"), estanteriaId),
                        cb.equal(destino.get("id"), estanteriaId)));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            }
            if (desde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            }
            if (hasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
