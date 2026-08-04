package com.empresa.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para registrar una venta con sus líneas. El usuario se toma del JWT,
 * el total y los subtotales se calculan en el servidor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearVentaRequest {

    @NotEmpty(message = "La venta debe tener al menos una línea")
    @Valid
    private List<CrearVentaDetalleRequest> detalles;
}
