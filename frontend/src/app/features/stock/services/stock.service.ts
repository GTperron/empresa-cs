import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse, Page } from '../../../core/models';
import { Stock, StockFiltros } from '../models/stock.model';

/** Consulta de stock (solo lectura; modificaciones vía Movimientos). */
@Injectable({ providedIn: 'root' })
export class StockService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/stock`;

  listar(
    filtros: StockFiltros,
    page: number,
    size: number,
  ): Observable<Page<Stock>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filtros.productoId !== null) {
      params = params.set('productoId', filtros.productoId);
    }
    if (filtros.estanteriaId !== null) {
      params = params.set('estanteriaId', filtros.estanteriaId);
    }
    if (filtros.almacenId !== null) {
      params = params.set('almacenId', filtros.almacenId);
    }
    return this.http
      .get<ApiResponse<Page<Stock>>>(this.baseUrl, { params })
      .pipe(map((r) => r.data));
  }

  listarPorProducto(productoId: number): Observable<Stock[]> {
    return this.http
      .get<ApiResponse<Stock[]>>(`${this.baseUrl}/producto/${productoId}`)
      .pipe(map((r) => r.data));
  }
}
