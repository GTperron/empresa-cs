package com.empresa.inventario.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la cantidad actual de un producto en una estantería concreta.
 * La combinación (producto, estantería) es única: si ya existe, se actualiza la cantidad.
 */
@Entity
@Table(
    name = "stock",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_stock_producto_estanteria",
        columnNames = {"producto_id", "estanteria_id"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estanteria_id", nullable = false)
    private Estanteria estanteria;

    // Cantidad actual; nunca negativa (garantizado por lógica de servicio + CHECK en BD).
    @Column(name = "cantidad", nullable = false, precision = 14, scale = 3)
    @Builder.Default
    private BigDecimal cantidad = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
