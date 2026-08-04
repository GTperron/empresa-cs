/**
 * Estantería (anidada bajo una zona). Espeja com.empresa.inventario.dto.EstanteriaDTO.
 * `descripcion` y `capacidadMaxima` son opcionales (backend usa @JsonInclude(NON_NULL)).
 */
export interface Estanteria {
  id: number;
  zonaId: number;
  codigo: string;
  nombre: string;
  descripcion?: string | null;
  capacidadMaxima?: number | null;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

/** POST /zonas/{zonaId}/estanterias (CrearEstanteriaRequest). */
export interface CrearEstanteriaRequest {
  codigo: string;
  nombre: string;
  descripcion?: string;
  capacidadMaxima?: number | null;
}

/** PUT /estanterias/{id} (EditarEstanteriaRequest). Mismo cuerpo que el de creación. */
export type EditarEstanteriaRequest = CrearEstanteriaRequest;
