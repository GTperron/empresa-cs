import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../core/services/auth.service';

interface NavItem {
  etiqueta: string;
  icono: string;
  ruta: string;
}

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly authService = inject(AuthService);

  readonly usuario = this.authService.currentUser;

  // Placeholders de los módulos que se irán implementando; por ahora van a "Próximamente".
  readonly navItems: NavItem[] = [
    { etiqueta: 'Almacenes', icono: 'warehouse', ruta: '/app/almacenes' },
    { etiqueta: 'Productos', icono: 'inventory_2', ruta: '/app/proximamente/productos' },
    { etiqueta: 'Stock', icono: 'inventory', ruta: '/app/proximamente/stock' },
    { etiqueta: 'Movimientos', icono: 'swap_horiz', ruta: '/app/proximamente/movimientos' },
    { etiqueta: 'Transformaciones', icono: 'transform', ruta: '/app/proximamente/transformaciones' },
    { etiqueta: 'Ventas', icono: 'point_of_sale', ruta: '/app/proximamente/ventas' },
  ];

  logout(): void {
    this.authService.logout();
  }
}
