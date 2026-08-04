import { HttpClient, HttpContext } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SILENCIAR_ERROR_GLOBAL } from '../interceptors/error.interceptor';
import {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegistroRequest,
  RestablecerContrasenaRequest,
  SolicitarRecuperacionRequest,
  Usuario,
} from '../models';
import { TokenStorageService } from './token-storage.service';

/**
 * Fuente de verdad de la sesión. Expone el usuario autenticado como signals y
 * concentra las llamadas de autenticación contra el backend.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly router = inject(Router);
  private readonly apiUrl = environment.apiUrl;

  // Estado reactivo. Se rehidrata desde localStorage al cargar la app (evita "parpadeo" de sesión).
  private readonly usuarioActual = signal<Usuario | null>(this.tokenStorage.getUsuario());

  /** Usuario autenticado (solo lectura para el resto de la app). */
  readonly currentUser = this.usuarioActual.asReadonly();
  /** ¿Hay sesión activa? */
  readonly isAuthenticated = computed(() => this.usuarioActual() !== null);
  /** ¿El usuario tiene rol ADMIN? */
  readonly isAdmin = computed(() => this.usuarioActual()?.roles.includes('ADMIN') ?? false);

  login(request: LoginRequest): Observable<AuthResponse> {
    // Silenciamos el snackbar global: el login muestra el error de credenciales inline.
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.apiUrl}/auth/login`, request, { context: silenciar() })
      .pipe(map((r) => r.data), tap((auth) => this.establecerSesion(auth)));
  }

  registro(request: RegistroRequest): Observable<AuthResponse> {
    // El backend devuelve tokens en el registro → queda logueado automáticamente.
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.apiUrl}/auth/registro`, request, { context: silenciar() })
      .pipe(map((r) => r.data), tap((auth) => this.establecerSesion(auth)));
  }

  solicitarRecuperacion(request: SolicitarRecuperacionRequest): Observable<string> {
    return this.http
      .post<ApiResponse<void>>(`${this.apiUrl}/auth/recuperar-contrasena`, request, { context: silenciar() })
      .pipe(map((r) => r.mensaje));
  }

  restablecerContrasena(request: RestablecerContrasenaRequest): Observable<string> {
    return this.http
      .post<ApiResponse<void>>(`${this.apiUrl}/auth/restablecer-contrasena`, request, { context: silenciar() })
      .pipe(map((r) => r.mensaje));
  }

  /**
   * Refresca el access token usando el refresh token guardado.
   * Silenciado a nivel global: el manejo de su fallo lo hace el authInterceptor
   * (que dispara sesionExpirada()); así evitamos un snackbar duplicado.
   */
  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('No hay refresh token disponible'));
    }
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.apiUrl}/auth/refresh`, { refreshToken }, { context: silenciar() })
      .pipe(map((r) => r.data), tap((auth) => this.establecerSesion(auth)));
  }

  /** Logout iniciado por el usuario: revoca el refresh token en el backend y limpia local. */
  logout(): void {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (refreshToken) {
      this.http
        .post<ApiResponse<void>>(`${this.apiUrl}/usuarios/logout`, { refreshToken }, { context: silenciar() })
        .subscribe({ next: () => undefined, error: () => undefined });
    }
    this.limpiarSesion();
    this.router.navigate(['/login']);
  }

  /** Llamado por el interceptor cuando el refresh falla: limpia la sesión y va al login. */
  sesionExpirada(): void {
    this.limpiarSesion();
    this.router.navigate(['/login']);
  }

  private establecerSesion(auth: AuthResponse): void {
    this.tokenStorage.setTokens(auth.accessToken, auth.refreshToken);
    this.tokenStorage.setUsuario(auth.usuario);
    this.usuarioActual.set(auth.usuario);
  }

  private limpiarSesion(): void {
    this.tokenStorage.clear();
    this.usuarioActual.set(null);
  }
}

/** Construye un HttpContext que desactiva el snackbar global para esta petición. */
function silenciar(): HttpContext {
  return new HttpContext().set(SILENCIAR_ERROR_GLOBAL, true);
}
