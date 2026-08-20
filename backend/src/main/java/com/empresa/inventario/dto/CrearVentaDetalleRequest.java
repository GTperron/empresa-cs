package com.empresa.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Línea de una venta: producto (tipo VENTA), estantería de descuento y cantidad.
 * El precio unitario se toma del producto en el servidor, no de este body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearVentaDetalleRequest {

    @NotNull(message = "El producto es requerido")
    private Long productoId;

    @NotNull(message = "La estantería es requerida")
    private Long estanteriaId;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;
}
