package com.empresa.inventario.repository.spec;

import com.empresa.inventario.entity.Transformacion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Predicados opcionales del historial de transformaciones.
 * Solo se agrega al WHERE el filtro que viene con valor.
 */
public final class TransformacionSpecs {

    private TransformacionSpecs() {
    }

    public static Specification<Transformacion> conFiltros(Long productoEntradaId,
                                                           Long usuarioId,
                                                           LocalDateTime desde,
                                                           LocalDateTime hasta) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productoEntradaId != null) {
                predicates.add(cb.equal(root.get("productoEntrada").get("id"), productoEntradaId));
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
