import { HttpErrorResponse } from '@angular/common/http';
import { ApiResponse } from '../../core/models';

/**
 * Extrae un mensaje legible de un error HTTP para mostrarlo inline en un formulario.
 * Prioriza el `mensaje` que envía el backend en el body ApiResponse.
 */
export function mensajeDeError(error: unknown, fallback = 'Ocurrió un error. Intentá nuevamente.'): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor. Verificá que el backend esté corriendo.';
    }
    const cuerpo = error.error as ApiResponse<unknown> | undefined;
    if (cuerpo?.mensaje) {
      return cuerpo.mensaje;
    }
  }
  return fallback;
}
