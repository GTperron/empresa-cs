package com.empresa.inventario.dto;

import com.empresa.inventario.enums.MovimientoTipo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO con información de un movimiento de stock (para respuestas).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovimientoStockDTO {

    private Long id;
    private Long productoId;
    private String productoCodigo;
    private MovimientoTipo tipo;
    private Long estanteriaId;
    private String estanteriaCodigo;
    private Long estanteriaDestinoId;
    private String estanteriaDestinoCodigo;
    private BigDecimal cantidad;
    private Long usuarioId;
    private String usuarioEmail;
    private String motivo;
    private LocalDateTime fecha;
    private LocalDateTime createdAt;
}
