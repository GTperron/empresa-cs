package com.empresa.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO con información de una zona (para respuestas).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZonaDTO {

    private Long id;
    private Long almacenId;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private Long cantidadEstanteriasActivas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
