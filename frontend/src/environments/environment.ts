/**
 * Configuración de entorno.
 * En desarrollo, apiUrl apunta a '/api', que el dev-server de Angular redirige
 * a http://localhost:8080/api vía proxy.conf.json (evita CORS en el navegador).
 * Para producción, reemplazar por la URL absoluta del backend.
 */
export const environment = {
  production: false,
  apiUrl: '/api',
};
