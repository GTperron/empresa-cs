import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SILENCIAR_ERROR_GLOBAL } from '../../../core/interceptors/error.interceptor';
import { ApiResponse, Page } from '../../../core/models';
import { Almacen, CrearAlmacenRequest, EditarAlmacenRequest } from '../models/almacen.model';

/**
 * Acceso a la API de almacenes. Feature-scoped: sólo lo usa el módulo de Almacenes,
 * así queda en el chunk lazy de la feature en vez del bundle inicial.
 *
 * Las operaciones de escritura (crear/editar/activar/desactivar) silencian el snackbar
 * global para que el componente maneje el error de forma específica (p. ej. 409 por
 * código duplicado en el diálogo, o por zonas activas al desactivar).
 */
@Injectable({ providedIn: 'root' })
export class AlmacenService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/almacenes`;

  listar(activo: boolean | null, page: number, size: number): Observable<Page<Almacen>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (activo !== null) {
      params = params.set('activo', activo);
    }
    return this.http
      .get<ApiResponse<Page<Almacen>>>(this.baseUrl, { params })
      .pipe(map((r) => r.data));
  }

  obtener(id: number): Observable<Almacen> {
    return this.http.get<ApiResponse<Almacen>>(`${this.baseUrl}/${id}`).pipe(map((r) => r.data));
  }

  crear(request: CrearAlmacenRequest): Observable<Almacen> {
    return this.http
      .post<ApiResponse<Almacen>>(this.baseUrl, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  editar(id: number, request: EditarAlmacenRequest): Observable<Almacen> {
    return this.http
      .put<ApiResponse<Almacen>>(`${this.baseUrl}/${id}`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  activar(id: number): Observable<Almacen> {
    return this.http
      .patch<ApiResponse<Almacen>>(`${this.baseUrl}/${id}/activar`, {}, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  desactivar(id: number): Observable<Almacen> {
    return this.http
      .patch<ApiResponse<Almacen>>(`${this.baseUrl}/${id}/desactivar`, {}, { context: silenciar() })
      .pipe(map((r) => r.data));
  }
}

/** Desactiva el snackbar global de error para esta petición (manejo inline en el componente). */
function silenciar(): HttpContext {
  return new HttpContext().set(SILENCIAR_ERROR_GLOBAL, true);
}
