import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SILENCIAR_ERROR_GLOBAL } from '../../../core/interceptors/error.interceptor';
import { ApiResponse, Page } from '../../../core/models';
import { CrearVentaRequest, Venta, VentaFiltros } from '../models/venta.model';

/**
 * Acceso a la API de ventas. La escritura silencia el snackbar global
 * para manejar el 409 (stock insuficiente, tipo inválido, etc.) inline.
 */
@Injectable({ providedIn: 'root' })
export class VentaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ventas`;

  listar(filtros: VentaFiltros, page: number, size: number): Observable<Page<Venta>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'fecha,desc');
    if (filtros.estado !== null) {
      params = params.set('estado', filtros.estado);
    }
    if (filtros.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    return this.http.get<ApiResponse<Page<Venta>>>(this.baseUrl, { params }).pipe(map((r) => r.data));
  }

  obtener(id: number): Observable<Venta> {
    return this.http.get<ApiResponse<Venta>>(`${this.baseUrl}/${id}`).pipe(map((r) => r.data));
  }

  crear(request: CrearVentaRequest): Observable<Venta> {
    return this.http
      .post<ApiResponse<Venta>>(this.baseUrl, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }
}

function silenciar(): HttpContext {
  return new HttpContext().set(SILENCIAR_ERROR_GLOBAL, true);
}
