package com.empresa.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de una transformación con su detalle completo (para respuestas).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformacionDTO {

    private Long id;
    private Long productoEntradaId;
    private String productoEntradaCodigo;
    private String productoEntradaNombre;
    private Long estanteriaOrigenId;
    private String estanteriaOrigenCodigo;
    private BigDecimal cantidadConsumida;
    private Long usuarioId;
    private String usuarioEmail;
    private LocalDateTime fecha;
    private String observaciones;
    private List<TransformacionDetalleDTO> detalles;
    private LocalDateTime createdAt;
}
