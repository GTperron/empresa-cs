package com.empresa.inventario.repository;

import com.empresa.inventario.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para consultar el histórico de movimientos de stock.
 * Los filtros opcionales van por {@link org.springframework.data.jpa.domain.Specification}.
 */
@Repository
public interface MovimientoStockRepository
        extends JpaRepository<MovimientoStock, Long>, JpaSpecificationExecutor<MovimientoStock> {
}
