/** Tipos expuestos por GET /movimientos y movimientos registrados vía API. */
export type MovimientoTipoHistorial = 'ENTRADA' | 'TRASLADO' | 'AJUSTE';

/** Espeja com.empresa.inventario.dto.MovimientoStockDTO */
export interface MovimientoStock {
  id: number;
  productoId: number;
  productoCodigo: string;
  tipo: MovimientoTipoHistorial;
  estanteriaId: number;
  estanteriaCodigo: string;
  estanteriaDestinoId?: number | null;
  estanteriaDestinoCodigo?: string | null;
  cantidad: number;
  usuarioId: number;
  usuarioEmail: string;
  motivo?: string | null;
  fecha: string;
  createdAt: string;
}

/** POST /movimientos/entrada */
export interface EntradaRequest {
  productoId: number;
  estanteriaId: number;
  cantidad: number;
  motivo?: string;
}

/** POST /movimientos/traslado */
export interface TrasladoRequest {
  productoId: number;
  estanteriaOrigenId: number;
  estanteriaDestinoId: number;
  cantidad: number;
  motivo?: string;
}

/** POST /movimientos/ajuste */
export interface AjusteRequest {
  productoId: number;
  estanteriaId: number;
  cantidad: number;
  motivo: string;
}

export interface MovimientoFiltros {
  productoId: number | null;
  estanteriaId: number | null;
  tipo: MovimientoTipoHistorial | null;
  desde: string | null;
  hasta: string | null;
}

export const TIPOS_MOVIMIENTO_FILTRO: MovimientoTipoHistorial[] = ['ENTRADA', 'TRASLADO', 'AJUSTE'];

export const ETIQUETAS_TIPO_MOVIMIENTO: Record<MovimientoTipoHistorial, string> = {
  ENTRADA: 'Entrada',
  TRASLADO: 'Traslado',
  AJUSTE: 'Ajuste',
};
