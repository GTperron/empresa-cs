package com.empresa.inventario.dto;

import com.empresa.inventario.enums.ProductoTipo;
import com.empresa.inventario.enums.UnidadMedida;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO con información de un producto (para respuestas).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private ProductoTipo tipo;
    private UnidadMedida unidadMedida;
    private BigDecimal precioVenta;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
