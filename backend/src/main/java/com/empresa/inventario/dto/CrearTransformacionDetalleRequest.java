package com.empresa.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Línea de una transformación: producto de venta generado y su estantería destino.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearTransformacionDetalleRequest {

    @NotNull(message = "El producto de venta es requerido")
    private Long productoVentaId;

    @NotNull(message = "La estantería de destino es requerida")
    private Long estanteriaDestinoId;

    @NotNull(message = "La cantidad generada es requerida")
    @Positive(message = "La cantidad generada debe ser mayor a cero")
    private BigDecimal cantidadGenerada;
}
