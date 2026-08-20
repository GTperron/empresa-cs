package com.empresa.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO con la cantidad de un producto en una ubicación concreta (estantería),
 * incluyendo la jerarquía hacia arriba (zona y almacén) para ubicarlo fácilmente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockDTO {

    private Long id;
    private Long productoId;
    private String productoCodigo;
    private String productoNombre;
    private Long estanteriaId;
    private String estanteriaCodigo;
    private Long zonaId;
    private String zonaCodigo;
    private Long almacenId;
    private String almacenCodigo;
    private BigDecimal cantidad;
    private LocalDateTime updatedAt;
}
