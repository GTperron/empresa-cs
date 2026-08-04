import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { mensajeDeError } from '../../../shared/utils/errores';
import { AlmacenService } from '../../almacenes/services/almacen.service';
import { ZonaForm } from '../zona-form/zona-form';
import { Zona } from '../models/zona.model';
import { ZonaService } from '../services/zona.service';

@Component({
  selector: 'app-zonas-list',
  imports: [
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTooltipModule,
  ],
  templateUrl: './zonas-list.html',
  styleUrl: './zonas-list.scss',
})
export class ZonasList {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(ZonaService);
  private readonly almacenService = inject(AlmacenService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly authService = inject(AuthService);

  readonly esAdmin = this.authService.isAdmin;
  readonly almacenId = Number(this.route.snapshot.paramMap.get('almacenId'));

  readonly almacenNombre = signal<string>('');
  readonly datos = signal<Zona[]>([]);
  readonly cargando = signal(false);
  readonly filtroActivo = signal<boolean | null>(null);

  // "Ver estanterías" es una acción de lectura → la columna se muestra siempre.
  readonly columnas = ['codigo', 'nombre', 'descripcion', 'estanterias', 'estado', 'acciones'];

  constructor() {
    this.almacenService.obtener(this.almacenId).subscribe({
      next: (a) => this.almacenNombre.set(`${a.codigo} — ${a.nombre}`),
      error: () => this.almacenNombre.set(`Almacén #${this.almacenId}`),
    });
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.service.listarPorAlmacen(this.almacenId, this.filtroActivo()).subscribe({
      next: (zonas) => {
        this.datos.set(zonas);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  cambiarFiltro(valor: boolean | null): void {
    this.filtroActivo.set(valor);
    this.cargar();
  }

  verEstanterias(zona: Zona): void {
    this.router.navigate(['/app/almacenes', this.almacenId, 'zonas', zona.id, 'estanterias']);
  }

  nuevo(): void {
    this.abrirDialogo(null);
  }

  editar(zona: Zona): void {
    this.abrirDialogo(zona);
  }

  activar(zona: Zona): void {
    this.service.activar(zona.id).subscribe({
      next: () => {
        this.snackBar.open('Zona activada', 'Cerrar', { duration: 3000 });
        this.cargar();
      },
      error: (err) => this.snackBar.open(mensajeDeError(err), 'Cerrar', { duration: 6000 }),
    });
  }

  desactivar(zona: Zona): void {
    this.service.desactivar(zona.id).subscribe({
      next: () => {
        this.snackBar.open('Zona desactivada', 'Cerrar', { duration: 3000 });
        this.cargar();
      },
      // 409 "La zona tiene estanterías activas": mostramos el mensaje específico del backend.
      error: (err) => this.snackBar.open(mensajeDeError(err), 'Cerrar', { duration: 6000 }),
    });
  }

  private abrirDialogo(zona: Zona | null): void {
    const ref = this.dialog.open(ZonaForm, {
      data: { almacenId: this.almacenId, zona },
      width: '480px',
    });
    ref.afterClosed().subscribe((guardado: boolean | undefined) => {
      if (guardado) {
        this.snackBar.open(zona ? 'Zona actualizada' : 'Zona creada', 'Cerrar', { duration: 3000 });
        this.cargar();
      }
    });
  }
}
