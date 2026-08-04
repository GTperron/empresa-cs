import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { ApiResponse } from '../models';

/**
 * Permite a una petición desactivar el snackbar global de error para manejarlo inline
 * (lo usan login/registro/refresh). Por defecto está desactivado (= mostrar snackbar).
 */
export const SILENCIAR_ERROR_GLOBAL = new HttpContextToken<boolean>(() => false);

/**
 * Interceptor externo: muestra un snackbar consistente ante cualquier error HTTP
 * (respuestas con exitoso:false llegan como status 4xx/5xx con { mensaje } en el body).
 * Va ANTES que authInterceptor en la cadena, de modo que el 401 con refresh exitoso
 * nunca llega hasta acá (no molesta al usuario con un toast durante el refresh silencioso).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!req.context.get(SILENCIAR_ERROR_GLOBAL)) {
        snackBar.open(extraerMensaje(error), 'Cerrar', {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: 'snackbar-error',
        });
      }
      return throwError(() => error);
    }),
  );
};

function extraerMensaje(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'No se pudo conectar con el servidor. Verificá que el backend esté corriendo.';
  }
  const cuerpo = error.error as ApiResponse<unknown> | undefined;
  if (cuerpo?.mensaje) {
    return cuerpo.mensaje;
  }
  return 'Ocurrió un error inesperado. Intentá nuevamente.';
}
