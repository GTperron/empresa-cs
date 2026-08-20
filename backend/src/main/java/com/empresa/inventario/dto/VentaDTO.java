package com.empresa.inventario.dto;

import com.empresa.inventario.enums.VentaEstado;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de una venta con su detalle completo (para respuestas).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VentaDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioEmail;
    private LocalDateTime fecha;
    private BigDecimal total;
    private VentaEstado estado;
    private List<VentaDetalleDTO> detalles;
    private LocalDateTime createdAt;
}
