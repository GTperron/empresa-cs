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
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de una transformación: consume un producto de tipo ENTRADA y genera
 * uno o varios productos de tipo VENTA (ver {@link TransformacionDetalle}).
 */
@Entity
@Table(name = "transformacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transformacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_entrada_id", nullable = false)
    private Producto productoEntrada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estanteria_origen_id", nullable = false)
    private Estanteria estanteriaOrigen;

    @Column(name = "cantidad_consumida", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidadConsumida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    // Agregado: los detalles se persisten en cascada junto con la cabecera.
    @OneToMany(mappedBy = "transformacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TransformacionDetalle> detalles = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    /** Agrega un detalle manteniendo la relación bidireccional consistente. */
    public void agregarDetalle(TransformacionDetalle detalle) {
        detalle.setTransformacion(this);
        this.detalles.add(detalle);
    }
}
