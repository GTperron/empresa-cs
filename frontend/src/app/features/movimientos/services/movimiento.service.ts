import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SILENCIAR_ERROR_GLOBAL } from '../../../core/interceptors/error.interceptor';
import { ApiResponse, Page } from '../../../core/models';
import {
  AjusteRequest,
  EntradaRequest,
  MovimientoFiltros,
  MovimientoStock,
  TrasladoRequest,
} from '../models/movimiento.model';

@Injectable({ providedIn: 'root' })
export class MovimientoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/movimientos`;

  listar(filtros: MovimientoFiltros, page: number, size: number): Observable<Page<MovimientoStock>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'fecha,desc');
    if (filtros.productoId !== null) {
      params = params.set('productoId', filtros.productoId);
    }
    if (filtros.estanteriaId !== null) {
      params = params.set('estanteriaId', filtros.estanteriaId);
    }
    if (filtros.tipo !== null) {
      params = params.set('tipo', filtros.tipo);
    }
    if (filtros.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    return this.http
      .get<ApiResponse<Page<MovimientoStock>>>(this.baseUrl, { params })
      .pipe(map((r) => r.data));
  }

  registrarEntrada(request: EntradaRequest): Observable<MovimientoStock> {
    return this.http
      .post<ApiResponse<MovimientoStock>>(`${this.baseUrl}/entrada`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  registrarTraslado(request: TrasladoRequest): Observable<MovimientoStock> {
    return this.http
      .post<ApiResponse<MovimientoStock>>(`${this.baseUrl}/traslado`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  registrarAjuste(request: AjusteRequest): Observable<MovimientoStock> {
    return this.http
      .post<ApiResponse<MovimientoStock>>(`${this.baseUrl}/ajuste`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }
}

function silenciar(): HttpContext {
  return new HttpContext().set(SILENCIAR_ERROR_GLOBAL, true);
}
