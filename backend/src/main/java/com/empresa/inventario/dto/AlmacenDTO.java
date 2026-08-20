package com.empresa.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO con información de un almacén (para respuestas).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlmacenDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private Boolean activo;
    private Long cantidadZonasActivas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
