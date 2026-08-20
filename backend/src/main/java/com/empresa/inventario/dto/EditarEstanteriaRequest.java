package com.empresa.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la edición de una estantería.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarEstanteriaRequest {

    @NotBlank(message = "El código es requerido")
    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    @PositiveOrZero(message = "La capacidad máxima no puede ser negativa")
    private Integer capacidadMaxima;
}
