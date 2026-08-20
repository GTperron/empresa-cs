package com.empresa.inventario.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entidad que representa una estantería dentro de una zona.
 * Nivel inferior de la jerarquía: Almacén -> Zona -> Estantería.
 * El código de estantería es único dentro de su zona (no globalmente).
 */
@Entity
@Table(
    name = "estanteria",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_estanteria_codigo_por_zona",
        columnNames = {"zona_id", "codigo"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estanteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación unidireccional muchos-a-uno con Zona.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @NotBlank(message = "El código es requerido")
    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    // Capacidad máxima opcional (nullable). Si se informa, no puede ser negativa.
    @PositiveOrZero(message = "La capacidad máxima no puede ser negativa")
    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

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
