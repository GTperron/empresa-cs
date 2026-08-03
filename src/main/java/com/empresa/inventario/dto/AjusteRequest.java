package com.empresa.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para registrar un movimiento de AJUSTE.
 * La cantidad lleva signo (positiva suma, negativa resta) y el motivo es obligatorio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjusteRequest {

    @NotNull(message = "El producto es requerido")
    private Long productoId;

    @NotNull(message = "La estantería es requerida")
    private Long estanteriaId;

    // Con signo: positiva suma, negativa resta. El valor 0 se rechaza en el servicio.
    @NotNull(message = "La cantidad es requerida")
    private BigDecimal cantidad;

    @NotBlank(message = "El motivo es obligatorio para un ajuste")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivo;
}
