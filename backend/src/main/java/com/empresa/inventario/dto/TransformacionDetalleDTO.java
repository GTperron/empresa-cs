package com.empresa.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO de una línea de transformación (para respuestas).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformacionDetalleDTO {

    private Long id;
    private Long productoVentaId;
    private String productoVentaCodigo;
    private String productoVentaNombre;
    private Long estanteriaDestinoId;
    private String estanteriaDestinoCodigo;
    private BigDecimal cantidadGenerada;
}
