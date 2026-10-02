package com.empresa.inventario.repository;

import com.empresa.inventario.entity.Transformacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para consultar el histórico de transformaciones.
 * Los filtros opcionales van por {@link org.springframework.data.jpa.domain.Specification}.
 */
@Repository
public interface TransformacionRepository
        extends JpaRepository<Transformacion, Long>, JpaSpecificationExecutor<Transformacion> {
}
