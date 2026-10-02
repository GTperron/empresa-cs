package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para consultar el histórico de ventas.
 * Los filtros opcionales van por {@link org.springframework.data.jpa.domain.Specification}.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long>, JpaSpecificationExecutor<Venta> {
}
