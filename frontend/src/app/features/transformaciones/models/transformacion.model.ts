/** Espeja com.empresa.inventario.dto.TransformacionDetalleDTO (respuesta). */
export interface TransformacionDetalle {
  id: number;
  productoVentaId: number;
  productoVentaCodigo: string;
  productoVentaNombre: string;
  estanteriaDestinoId: number;
  estanteriaDestinoCodigo: string;
  cantidadGenerada: number;
}

/** Espeja com.empresa.inventario.dto.TransformacionDTO (respuesta). */
export interface Transformacion {
  id: number;
  productoEntradaId: number;
  productoEntradaCodigo: string;
  productoEntradaNombre: string;
  estanteriaOrigenId: number;
  estanteriaOrigenCodigo: string;
  cantidadConsumida: number;
  usuarioId: number;
  usuarioEmail: string;
  fecha: string;
  observaciones?: string | null;
  detalles: TransformacionDetalle[];
  createdAt: string;
}

/** Línea de CrearTransformacionRequest (espeja CrearTransformacionDetalleRequest). */
export interface CrearTransformacionDetalleRequest {
  productoVentaId: number;
  estanteriaDestinoId: number;
  cantidadGenerada: number;
}

/** POST /transformaciones (espeja CrearTransformacionRequest). */
export interface CrearTransformacionRequest {
  productoEntradaId: number;
  estanteriaOrigenId: number;
  cantidadConsumida: number;
  observaciones?: string;
  detalles: CrearTransformacionDetalleRequest[];
}

/** Filtros opcionales de GET /transformaciones. */
export interface TransformacionFiltros {
  productoEntradaId: number | null;
  desde: string | null;
  hasta: string | null;
}
