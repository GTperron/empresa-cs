package com.empresa.inventario.dto;

import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.enums.UnidadMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para la edición de un producto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarProductoRequest {

    @NotBlank(message = "El código es requerido")
    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo es requerido")
    private ProductoTipo tipo;

    @NotNull(message = "La unidad de medida es requerida")
    private UnidadMedida unidadMedida;

    @PositiveOrZero(message = "El precio de venta no puede ser negativo")
    private BigDecimal precioVenta;
}
