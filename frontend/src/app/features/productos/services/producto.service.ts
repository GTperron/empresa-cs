import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SILENCIAR_ERROR_GLOBAL } from '../../../core/interceptors/error.interceptor';
import { ApiResponse, Page } from '../../../core/models';
import {
  CrearProductoRequest,
  EditarProductoRequest,
  Producto,
  ProductoTipo,
} from '../models/producto.model';

/**
 * Acceso a la API de productos. Feature-scoped: sólo lo usa el módulo de Productos.
 * Las operaciones de escritura silencian el snackbar global para manejo inline (409, etc.).
 */
@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/productos`;

  listar(
    tipo: ProductoTipo | null,
    activo: boolean | null,
    page: number,
    size: number,
  ): Observable<Page<Producto>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (tipo !== null) {
      params = params.set('tipo', tipo);
    }
    if (activo !== null) {
      params = params.set('activo', activo);
    }
    return this.http
      .get<ApiResponse<Page<Producto>>>(this.baseUrl, { params })
      .pipe(map((r) => r.data));
  }

  obtener(id: number): Observable<Producto> {
    return this.http.get<ApiResponse<Producto>>(`${this.baseUrl}/${id}`).pipe(map((r) => r.data));
  }

  crear(request: CrearProductoRequest): Observable<Producto> {
    return this.http
      .post<ApiResponse<Producto>>(this.baseUrl, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  editar(id: number, request: EditarProductoRequest): Observable<Producto> {
    return this.http
      .put<ApiResponse<Producto>>(`${this.baseUrl}/${id}`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  activar(id: number): Observable<Producto> {
    return this.http
      .patch<ApiResponse<Producto>>(`${this.baseUrl}/${id}/activar`, {}, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  desactivar(id: number): Observable<Producto> {
    return this.http
      .patch<ApiResponse<Producto>>(`${this.baseUrl}/${id}/desactivar`, {}, { context: silenciar() })
      .pipe(map((r) => r.data));
  }
}

function silenciar(): HttpContext {
  return new HttpContext().set(SILENCIAR_ERROR_GLOBAL, true);
}
