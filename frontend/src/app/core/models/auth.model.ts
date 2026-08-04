import { Usuario } from './usuario.model';

/** POST /auth/login */
export interface LoginRequest {
  email: string;
  password: string;
}

/** POST /auth/registro */
export interface RegistroRequest {
  email: string;
  password: string;
  passwordConfirmacion: string;
  nombre: string;
  apellido: string;
}

/** POST /auth/refresh y POST /usuarios/logout comparten este body. */
export interface RefreshTokenRequest {
  refreshToken: string;
}

/** POST /auth/recuperar-contrasena */
export interface SolicitarRecuperacionRequest {
  email: string;
}

/** POST /auth/restablecer-contrasena */
export interface RestablecerContrasenaRequest {
  token: string;
  nuevaContrasena: string;
  confirmacion: string;
}

/**
 * Payload de login/registro/refresh (dentro de ApiResponse.data).
 * Espeja com.empresa.inventario.dto.AuthResponse.
 */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  /** Segundos hasta que expira el accessToken (15 min = 900). */
  expiresIn: number;
  usuario: Usuario;
}
