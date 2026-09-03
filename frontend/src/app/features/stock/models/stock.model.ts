/** Espeja com.empresa.inventario.dto.StockDTO */
export interface Stock {
  id: number;
  productoId: number;
  productoCodigo: string;
  productoNombre: string;
  estanteriaId: number;
  estanteriaCodigo: string;
  zonaId: number;
  zonaCodigo: string;
  almacenId: number;
  almacenCodigo: string;
  cantidad: number;
  updatedAt: string;
}

export interface StockFiltros {
  productoId: number | null;
  estanteriaId: number | null;
  almacenId: number | null;
}
