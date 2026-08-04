package com.empresa.inventario.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Línea de una transformación: un producto de tipo VENTA generado, con su cantidad
 * y la estantería de destino donde queda disponible.
 */
@Entity
@Table(name = "transformacion_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransformacionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transformacion_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Transformacion transformacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_venta_id", nullable = false)
    private Producto productoVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estanteria_destino_id", nullable = false)
    private Estanteria estanteriaDestino;

    @Column(name = "cantidad_generada", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidadGenerada;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
