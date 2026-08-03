package com.empresa.inventario.entity;

import com.empresa.inventario.enums.MovimientoTipo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un movimiento de stock (histórico inmutable).
 * Para TRASLADO, estanteria = origen y estanteriaDestino = destino.
 */
@Entity
@Table(name = "movimiento_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Estantería de origen (o única, para ENTRADA/AJUSTE).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estanteria_id", nullable = false)
    private Estanteria estanteria;

    // Solo para TRASLADO: estantería de destino.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estanteria_destino_id")
    private Estanteria estanteriaDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private MovimientoTipo tipo;

    // Cantidad del movimiento. Positiva para ENTRADA/TRASLADO; con signo para AJUSTE.
    @Column(name = "cantidad", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidad;

    // Usuario que ejecutó el movimiento (tomado del JWT, no del cliente).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Obligatorio para AJUSTE; opcional para el resto.
    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
