package com.empresa.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para registrar un movimiento de ENTRADA.
 * El usuario que ejecuta el movimiento se toma del JWT, no de este body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntradaRequest {

    @NotNull(message = "El producto es requerido")
    private Long productoId;

    @NotNull(message = "La estantería es requerida")
    private Long estanteriaId;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivo;
}
