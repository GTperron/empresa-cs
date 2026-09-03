/** Espeja com.empresa.inventario.enums.ProductoTipo */
export type ProductoTipo = 'ENTRADA' | 'VENTA';

/** Espeja com.empresa.inventario.enums.UnidadMedida */
export type UnidadMedida =
  | 'UNIDAD'
  | 'KG'
  | 'GRAMO'
  | 'LITRO'
  | 'MILILITRO'
  | 'METRO'
  | 'CAJA';

/** Espeja com.empresa.inventario.dto.ProductoDTO */
export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion?: string | null;
  tipo: ProductoTipo;
  unidadMedida: UnidadMedida;
  precioVenta?: number | null;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

/** POST /productos (CrearProductoRequest). */
export interface CrearProductoRequest {
  codigo: string;
  nombre: string;
  descripcion?: string;
  tipo: ProductoTipo;
  unidadMedida: UnidadMedida;
  precioVenta?: number;
}

/** PUT /productos/{id} (EditarProductoRequest). Mismo cuerpo que el de creación. */
export type EditarProductoRequest = CrearProductoRequest;

export const TIPOS_PRODUCTO: ProductoTipo[] = ['ENTRADA', 'VENTA'];

export const UNIDADES_MEDIDA: UnidadMedida[] = [
  'UNIDAD',
  'KG',
  'GRAMO',
  'LITRO',
  'MILILITRO',
  'METRO',
  'CAJA',
];

export const ETIQUETAS_TIPO: Record<ProductoTipo, string> = {
  ENTRADA: 'Entrada',
  VENTA: 'Venta',
};

export const ETIQUETAS_UNIDAD: Record<UnidadMedida, string> = {
  UNIDAD: 'Unidad',
  KG: 'Kg',
  GRAMO: 'Gramo',
  LITRO: 'Litro',
  MILILITRO: 'Mililitro',
  METRO: 'Metro',
  CAJA: 'Caja',
};
