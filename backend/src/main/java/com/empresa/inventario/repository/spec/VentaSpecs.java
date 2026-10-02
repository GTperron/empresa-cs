package com.empresa.inventario.repository.spec;

import com.empresa.inventario.entity.Venta;
import com.empresa.inventario.enums.VentaEstado;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Predicados opcionales del historial de ventas.
 * Solo se agrega al WHERE el filtro que viene con valor.
 */
public final class VentaSpecs {

    private VentaSpecs() {
    }

    public static Specification<Venta> conFiltros(Long usuarioId,
                                                  VentaEstado estado,
                                                  LocalDateTime desde,
                                                  LocalDateTime hasta) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
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
