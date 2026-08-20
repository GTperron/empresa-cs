package com.empresa.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para registrar un movimiento de TRASLADO entre estanterías
 * (pueden pertenecer a almacenes distintos).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrasladoRequest {

    @NotNull(message = "El producto es requerido")
    private Long productoId;

    @NotNull(message = "La estantería de origen es requerida")
    private Long estanteriaOrigenId;

    @NotNull(message = "La estantería de destino es requerida")
    private Long estanteriaDestinoId;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivo;
}
