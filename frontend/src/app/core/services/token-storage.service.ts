import { Injectable } from '@angular/core';
import { Usuario } from '../models';

const ACCESS_TOKEN_KEY = 'inv.accessToken';
const REFRESH_TOKEN_KEY = 'inv.refreshToken';
const USUARIO_KEY = 'inv.usuario';

/**
 * Aísla el acceso al almacenamiento de tokens/usuario.
 * Usamos localStorage porque el refreshToken vive 30 días y queremos que la sesión
 * persista entre reinicios del navegador (sessionStorage se borraría al cerrar la pestaña).
 * Al estar centralizado acá, cambiar la estrategia (p. ej. cookies httpOnly) toca un solo lugar.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  getUsuario(): Usuario | null {
    const raw = localStorage.getItem(USUARIO_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as Usuario;
    } catch {
      return null;
    }
  }

  setUsuario(usuario: Usuario): void {
    localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USUARIO_KEY);
  }
}
