/**
 * Almacén. Espeja com.empresa.inventario.dto.AlmacenDTO.
 * `direccion` es opcional porque el backend usa @JsonInclude(NON_NULL) y la omite si es null.
 */
export interface Almacen {
  id: number;
  codigo: string;
  nombre: string;
  direccion?: string | null;
  activo: boolean;
  cantidadZonasActivas: number;
  createdAt: string;
  updatedAt: string;
}

/** POST /almacenes (CrearAlmacenRequest). */
export interface CrearAlmacenRequest {
  codigo: string;
  nombre: string;
  direccion?: string;
}

/** PUT /almacenes/{id} (EditarAlmacenRequest). Mismo cuerpo que el de creación. */
export type EditarAlmacenRequest = CrearAlmacenRequest;
