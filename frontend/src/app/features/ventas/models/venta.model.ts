/** Espeja com.empresa.inventario.enums.VentaEstado */
export type VentaEstado = 'COMPLETADA' | 'ANULADA';

/** Espeja com.empresa.inventario.dto.VentaDetalleDTO (respuesta). */
export interface VentaDetalle {
  id: number;
  productoId: number;
  productoCodigo: string;
  productoNombre: string;
  estanteriaId: number;
  estanteriaCodigo: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

/** Espeja com.empresa.inventario.dto.VentaDTO (respuesta). */
export interface Venta {
  id: number;
  usuarioId: number;
  usuarioEmail: string;
  fecha: string;
  total: number;
  estado: VentaEstado;
  detalles: VentaDetalle[];
  createdAt: string;
}

/** Línea de CrearVentaRequest (espeja CrearVentaDetalleRequest). */
export interface CrearVentaDetalleRequest {
  productoId: number;
  estanteriaId: number;
  cantidad: number;
}

/** POST /ventas (espeja CrearVentaRequest). Sin precio ni total. */
export interface CrearVentaRequest {
  detalles: CrearVentaDetalleRequest[];
}

/** Filtros opcionales de GET /ventas. */
export interface VentaFiltros {
  estado: VentaEstado | null;
  desde: string | null;
  hasta: string | null;
}

export const ESTADOS_VENTA: VentaEstado[] = ['COMPLETADA', 'ANULADA'];

export const ETIQUETAS_ESTADO_VENTA: Record<VentaEstado, string> = {
  COMPLETADA: 'Completada',
  ANULADA: 'Anulada',
};
