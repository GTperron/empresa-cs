import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // Angular Material 22 usa animaciones CSS; no requiere @angular/animations.
    // El orden importa: errorInterceptor queda "por fuera" de authInterceptor, de modo
    // que el 401 que dispara el refresh es capturado por auth ANTES de llegar al snackbar.
    provideHttpClient(withInterceptors([errorInterceptor, authInterceptor])),
  ],
};
