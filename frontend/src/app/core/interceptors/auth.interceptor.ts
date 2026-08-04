import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, catchError, filter, Observable, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';

/**
 * Estado compartido entre invocaciones del interceptor para coordinar el refresh:
 * si llegan varias peticiones con 401 a la vez, sólo se dispara UN refresh y las
 * demás esperan el nuevo token en lugar de refrescar en paralelo.
 */
let refrescando = false;
const tokenRefrescado$ = new BehaviorSubject<string | null>(null);

/**
 * Interceptor interno:
 *  - agrega el header Authorization a las peticiones protegidas,
 *  - ante un 401, intenta refrescar el token UNA vez y reintenta la petición original,
 *  - si el refresh falla, cierra la sesión y redirige al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const authService = inject(AuthService);

  // Los endpoints públicos de /auth no llevan token ni deben disparar refresh
  // (esto además corta cualquier recursión: el propio POST /auth/refresh pasa por acá).
  if (esEndpointPublicoDeAuth(req.url)) {
    return next(req);
  }

  const accessToken = tokenStorage.getAccessToken();
  const reqConToken = accessToken ? agregarToken(req, accessToken) : req;

  return next(reqConToken).pipe(
    catchError((error: HttpErrorResponse) => {
      // Sólo intentamos refrescar si es 401 y realmente tenemos un refresh token.
      if (error.status === 401 && tokenStorage.getRefreshToken()) {
        return manejar401(reqConToken, next, authService);
      }
      return throwError(() => error);
    }),
  );
};

function manejar401(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService,
): Observable<HttpEvent<unknown>> {
  if (!refrescando) {
    // Somos el primer 401: disparamos el refresh y bloqueamos a los siguientes.
    refrescando = true;
    tokenRefrescado$.next(null);

    return authService.refreshToken().pipe(
      switchMap((auth) => {
        refrescando = false;
        // Despertamos a las peticiones en cola con el token nuevo.
        tokenRefrescado$.next(auth.accessToken);
        // Reintentamos la petición original UNA vez con el token fresco.
        return next(agregarToken(req, auth.accessToken));
      }),
      catchError((err) => {
        // El refresh falló (refresh token vencido/revocado): cerramos sesión.
        refrescando = false;
        authService.sesionExpirada();
        return throwError(() => err);
      }),
    );
  }

  // Ya hay un refresh en curso: esperamos a que emita el token nuevo y reintentamos.
  return tokenRefrescado$.pipe(
    filter((token): token is string => token !== null),
    take(1),
    switchMap((token) => next(agregarToken(req, token))),
  );
}

function agregarToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

function esEndpointPublicoDeAuth(url: string): boolean {
  return (
    url.includes('/auth/login') ||
    url.includes('/auth/registro') ||
    url.includes('/auth/refresh') ||
    url.includes('/auth/recuperar-contrasena') ||
    url.includes('/auth/restablecer-contrasena')
  );
}
