package com.empresa.inventario.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entidad que representa una zona dentro de un almacén.
 * Nivel intermedio de la jerarquía: Almacén -> Zona -> Estantería.
 * El código de zona es único dentro de su almacén (no globalmente).
 */
@Entity
@Table(
    name = "zona",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_zona_codigo_por_almacen",
        columnNames = {"almacen_id", "codigo"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación unidireccional muchos-a-uno con Almacén.
    // No cargamos la colección de hijos desde el padre: las validaciones de
    // jerarquía se hacen con queries de conteo en el repositorio.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;

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
