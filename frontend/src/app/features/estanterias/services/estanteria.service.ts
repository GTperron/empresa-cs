import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SILENCIAR_ERROR_GLOBAL } from '../../../core/interceptors/error.interceptor';
import { ApiResponse } from '../../../core/models';
import { CrearEstanteriaRequest, EditarEstanteriaRequest, Estanteria } from '../models/estanteria.model';

/**
 * Acceso a la API de estanterías. El listado NO pagina (List<EstanteriaDTO>).
 * Escrituras silenciadas del snackbar global para manejo específico del error.
 */
@Injectable({ providedIn: 'root' })
export class EstanteriaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listarPorZona(zonaId: number, activo: boolean | null): Observable<Estanteria[]> {
    let params = new HttpParams();
    if (activo !== null) {
      params = params.set('activo', activo);
    }
    return this.http
      .get<ApiResponse<Estanteria[]>>(`${this.apiUrl}/zonas/${zonaId}/estanterias`, { params })
      .pipe(map((r) => r.data));
  }

  obtener(id: number): Observable<Estanteria> {
    return this.http.get<ApiResponse<Estanteria>>(`${this.apiUrl}/estanterias/${id}`).pipe(map((r) => r.data));
  }

  crear(zonaId: number, request: CrearEstanteriaRequest): Observable<Estanteria> {
    return this.http
      .post<ApiResponse<Estanteria>>(`${this.apiUrl}/zonas/${zonaId}/estanterias`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  editar(id: number, request: EditarEstanteriaRequest): Observable<Estanteria> {
    return this.http
      .put<ApiResponse<Estanteria>>(`${this.apiUrl}/estanterias/${id}`, request, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  activar(id: number): Observable<Estanteria> {
    return this.http
      .patch<ApiResponse<Estanteria>>(`${this.apiUrl}/estanterias/${id}/activar`, {}, { context: silenciar() })
      .pipe(map((r) => r.data));
  }

  desactivar(id: number): Observable<Estanteria> {
    return this.http
      .patch<ApiResponse<Estanteria>>(`${this.apiUrl}/estanterias/${id}/desactivar`, {}, { context: silenciar() })
      .pipe(map((r) => r.data));
  }
}

function silenciar(): HttpContext {
  return new HttpContext().set(SILENCIAR_ERROR_GLOBAL, true);
}
