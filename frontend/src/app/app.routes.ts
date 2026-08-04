import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'app' },

  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'registro',
    loadComponent: () => import('./features/auth/registro/registro').then((m) => m.Registro),
  },
  {
    path: 'recuperar-contrasena',
    loadComponent: () =>
      import('./features/auth/recuperar-contrasena/recuperar-contrasena').then((m) => m.RecuperarContrasena),
  },

  {
    path: 'app',
    canActivate: [authGuard],
    loadComponent: () => import('./features/layout/shell/shell').then((m) => m.Shell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      {
        path: 'inicio',
        loadComponent: () =>
          import('./features/layout/proximamente/proximamente').then((m) => m.Proximamente),
      },
      {
        path: 'almacenes',
        loadComponent: () =>
          import('./features/almacenes/almacenes-list/almacenes-list').then((m) => m.AlmacenesList),
      },
      {
        path: 'almacenes/:almacenId/zonas',
        loadComponent: () =>
          import('./features/zonas/zonas-list/zonas-list').then((m) => m.ZonasList),
      },
      {
        path: 'almacenes/:almacenId/zonas/:zonaId/estanterias',
        loadComponent: () =>
          import('./features/estanterias/estanterias-list/estanterias-list').then(
            (m) => m.EstanteriasList,
          ),
      },
      {
        path: 'proximamente/:modulo',
        loadComponent: () =>
          import('./features/layout/proximamente/proximamente').then((m) => m.Proximamente),
      },
    ],
  },

  { path: '**', redirectTo: 'app' },
];
