import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SILENCIAR_ERROR_GLOBAL } from '../../../core/interceptors/error.interceptor';
import { ApiResponse } from '../../../core/models';
import { CrearZonaRequest, EditarZonaRequest, Zona } from '../models/zona.model';

/**
 * Acceso a la API de zonas. El listado NO pagina (el backend devuelve List<ZonaDTO>).
 * Las operaciones de escritura silencian el snackbar global para manejo específico
 * del error (409 por código duplicado en el almacén, o por estanterías activas).
 */
@Injectable({ providedIn: 'root' })
export class ZonaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listarPorAlmacen(almacenId: number, activo: boolean | null): Observable<Zona[]> {
    let params = new HttpParams();
    if (activo !== null) {
      params = params.set('activo', activo);
    }
    return this.http
      .get<ApiResponse<Zona[]>>(`${this.apiUrl}/almacenes/${almacenId}/zonas`, { params })
      .pipe(map((r) => r.data));
  }

  obtener(id: number): Observable<Zona> {
    return this.http.get<ApiResponse<Zona>>(`${this.apiUrl}/zonas/${id}`).pipe(map((r) => r.data));
  }

  crear(almacenId: number, request: CrearZonaRequest): Observable<Zona> {
    return this.http
      .post<ApiResponse<Zona>>(`${this.apiUrl}/almacenes/${almacenId}/zonas`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  editar(id: number, request: EditarZonaRequest): Observable<Zona> {
    return this.http
      .put<ApiResponse<Zona>>(`${this.apiUrl}/zonas/${id}`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  activar(id: number): Observable<Zona> {
    return this.http
      .patch<ApiResponse<Zona>>(`${this.apiUrl}/zonas/${id}/activar`, {}, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  desactivar(id: number): Observable<Zona> {
    return this.http
      .patch<ApiResponse<Zona>>(`${this.apiUrl}/zonas/${id}/desactivar`, {}, { context: silenciar() })
      .pipe(map((r) => r.data));
  }
}

function silenciar(): HttpContext {
  return new HttpContext().set(SILENCIAR_ERROR_GLOBAL, true);
}
