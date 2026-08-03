package com.empresa.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar contraseña.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarContrasenaRequest {

    @NotBlank(message = "La contraseña actual es requerida")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String nuevaContrasena;

    @NotBlank(message = "La confirmación de contraseña es requerida")
    private String confirmacion;
}
