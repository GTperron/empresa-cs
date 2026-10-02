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
        path: 'stock',
        loadComponent: () =>
          import('./features/stock/stock-list/stock-list').then((m) => m.StockList),
      },
      {
        path: 'productos',
        loadComponent: () =>
          import('./features/productos/productos-list/productos-list').then((m) => m.ProductosList),
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
        path: 'movimientos',
        loadComponent: () =>
          import('./features/movimientos/movimientos-list/movimientos-list').then((m) => m.MovimientosList),
      },
      {
        path: 'transformaciones',
        loadComponent: () =>
          import('./features/transformaciones/transformaciones-list/transformaciones-list').then(
            (m) => m.TransformacionesList,
          ),
      },
      {
        path: 'transformaciones/nueva',
        loadComponent: () =>
          import('./features/transformaciones/transformacion-form/transformacion-form').then(
            (m) => m.TransformacionForm,
          ),
      },
      {
        path: 'transformaciones/:id',
        loadComponent: () =>
          import('./features/transformaciones/transformacion-detalle/transformacion-detalle').then(
            (m) => m.TransformacionDetalle,
          ),
      },
      {
        path: 'ventas',
        loadComponent: () =>
          import('./features/ventas/ventas-list/ventas-list').then((m) => m.VentasList),
      },
      {
        path: 'ventas/nueva',
        loadComponent: () =>
          import('./features/ventas/venta-form/venta-form').then((m) => m.VentaForm),
      },
      {
        path: 'ventas/:id',
        loadComponent: () =>
          import('./features/ventas/venta-detalle/venta-detalle').then((m) => m.VentaDetalle),
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
