import { BreakpointObserver } from '@angular/cdk/layout';
import {
  afterNextRender,
  Component,
  DestroyRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../core/services/auth.service';

/** Altura del toolbar en mobile (Material default). Evita ocultar el 1er ítem del sidenav con fixedInViewport. */
export const SHELL_TOOLBAR_MOBILE_HEIGHT = 56;

/** Mismo criterio que @media (max-width: 767.98px) en styles.scss (mobile/tablet angosto). */
export const SHELL_MOBILE_BREAKPOINT = '(max-width: 767.98px)';

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
export class Shell implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly breakpointObserver = inject(BreakpointObserver);
  private readonly destroyRef = inject(DestroyRef);

  @ViewChild('sidenav') sidenav?: MatSidenav;

  readonly usuario = this.authService.currentUser;

  readonly toolbarMobileHeight = SHELL_TOOLBAR_MOBILE_HEIGHT;

  /** true en viewport ≤767px → sidenav modo over, cerrado por defecto. */
  readonly esPantallaAngosta = signal(
    typeof globalThis.matchMedia !== 'undefined' &&
      globalThis.matchMedia(SHELL_MOBILE_BREAKPOINT).matches,
  );

  constructor() {
    afterNextRender(() => {
      this.syncSidenavAlViewport(this.esPantallaAngosta());
    });
  }

  ngOnInit(): void {
    document.body.classList.add('app-shell');

    this.breakpointObserver
      .observe(SHELL_MOBILE_BREAKPOINT)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ matches }) => {
        this.esPantallaAngosta.set(matches);
        this.syncSidenavAlViewport(matches);
      });
  }

  ngOnDestroy(): void {
    document.body.classList.remove('app-shell');
  }

  alternarMenu(): void {
    if (!this.esViewportAngosto()) {
      return;
    }
    void this.sidenav?.toggle();
  }

  cerrarSidenavSiMobile(): void {
    if (this.esViewportAngosto()) {
      void this.sidenav?.close();
    }
  }

  private esViewportAngosto(): boolean {
    return (
      this.esPantallaAngosta() ||
      (typeof globalThis.matchMedia !== 'undefined' &&
        globalThis.matchMedia(SHELL_MOBILE_BREAKPOINT).matches)
    );
  }

  private syncSidenavAlViewport(angosto: boolean): void {
    if (!this.sidenav) {
      return;
    }
    if (angosto) {
      void this.sidenav.close();
    } else {
      void this.sidenav.open();
    }
  }

  readonly navItems: NavItem[] = [
    { etiqueta: 'Almacenes', icono: 'warehouse', ruta: '/app/almacenes' },
    { etiqueta: 'Productos', icono: 'inventory_2', ruta: '/app/productos' },
    { etiqueta: 'Stock', icono: 'inventory', ruta: '/app/stock' },
    { etiqueta: 'Movimientos', icono: 'swap_horiz', ruta: '/app/proximamente/movimientos' },
    { etiqueta: 'Transformaciones', icono: 'transform', ruta: '/app/proximamente/transformaciones' },
    { etiqueta: 'Ventas', icono: 'point_of_sale', ruta: '/app/proximamente/ventas' },
  ];

  logout(): void {
    this.authService.logout();
  }
}
