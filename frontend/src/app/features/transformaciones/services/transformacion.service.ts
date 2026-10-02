import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SILENCIAR_ERROR_GLOBAL } from '../../../core/interceptors/error.interceptor';
import { ApiResponse, Page } from '../../../core/models';
import {
  CrearTransformacionRequest,
  Transformacion,
  TransformacionFiltros,
} from '../models/transformacion.model';

/**
 * Acceso a la API de transformaciones (insumo ENTRADA → uno o varios productos VENTA).
 * La escritura silencia el snackbar global para manejar el 409 inline en el formulario.
 */
@Injectable({ providedIn: 'root' })
export class TransformacionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/transformaciones`;

  listar(filtros: TransformacionFiltros, page: number, size: number): Observable<Page<Transformacion>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'fecha,desc');
    if (filtros.productoEntradaId !== null) {
      params = params.set('productoEntradaId', filtros.productoEntradaId);
    }
    if (filtros.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    return this.http
      .get<ApiResponse<Page<Transformacion>>>(this.baseUrl, { params })
      .pipe(map((r) => r.data));
  }

  obtener(id: number): Observable<Transformacion> {
    return this.http
      .get<ApiResponse<Transformacion>>(`${this.baseUrl}/${id}`)
      .pipe(map((r) => r.data));
  }

  crear(request: CrearTransformacionRequest): Observable<Transformacion> {
    return this.http
      .post<ApiResponse<Transformacion>>(this.baseUrl, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }
}

function silenciar(): HttpContext {
  return new HttpContext().set(SILENCIAR_ERROR_GLOBAL, true);
}
