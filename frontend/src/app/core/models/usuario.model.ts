/** Roles soportados por el backend. */
export type Rol = 'USER' | 'ADMIN';

/**
 * Usuario autenticado. Espeja com.empresa.inventario.dto.UsuarioDTO.
 * Las fechas llegan como string ISO-8601 (no como Date).
 */
export interface Usuario {
  id: number;
  email: string;
  nombre: string;
  apellido: string;
  activo: boolean;
  /** roles es un Set<String> en el backend; llega como array JSON. */
  roles: Rol[];
  ultimoLogin: string | null;
  createdAt: string;
  updatedAt: string;
}
