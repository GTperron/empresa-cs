package com.empresa.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para registrar una transformación (cabecera + líneas de producto de venta generado).
 * El usuario se toma del JWT, no de este body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearTransformacionRequest {

    @NotNull(message = "El producto de entrada es requerido")
    private Long productoEntradaId;

    @NotNull(message = "La estantería de origen es requerida")
    private Long estanteriaOrigenId;

    @NotNull(message = "La cantidad consumida es requerida")
    @Positive(message = "La cantidad consumida debe ser mayor a cero")
    private BigDecimal cantidadConsumida;

    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
    private String observaciones;

    @NotEmpty(message = "Debe indicar al menos un producto de venta generado")
    @Valid
    private List<CrearTransformacionDetalleRequest> detalles;
}
