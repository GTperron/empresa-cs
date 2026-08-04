/**
 * Envoltorio genérico de todas las respuestas del backend.
 * Espeja com.empresa.inventario.dto.ApiResponse<T>.
 */
export interface ApiResponse<T> {
  exitoso: boolean;
  mensaje: string;
  /** Presente en respuestas exitosas; en errores de validación es un mapa { campo: mensaje }. */
  data: T;
  /** ISO-8601 (LocalDateTime serializado por Jackson). */
  timestamp: string;
}

/**
 * Cuando exitoso = false por errores de validación, el backend devuelve
 * `data` como un mapa de { nombreDelCampo: mensajeDeError }.
 */
export type ErroresValidacion = Record<string, string>;
