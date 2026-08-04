/**
 * Zona (anidada bajo un almacén). Espeja com.empresa.inventario.dto.ZonaDTO.
 * `descripcion` es opcional (backend usa @JsonInclude(NON_NULL)).
 */
export interface Zona {
  id: number;
  almacenId: number;
  codigo: string;
  nombre: string;
  descripcion?: string | null;
  activo: boolean;
  cantidadEstanteriasActivas: number;
  createdAt: string;
  updatedAt: string;
}

/** POST /almacenes/{almacenId}/zonas (CrearZonaRequest). */
export interface CrearZonaRequest {
  codigo: string;
  nombre: string;
  descripcion?: string;
}

/** PUT /zonas/{id} (EditarZonaRequest). Mismo cuerpo que el de creación. */
export type EditarZonaRequest = CrearZonaRequest;
