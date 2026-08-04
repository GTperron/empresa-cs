import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { mensajeDeError } from '../../../shared/utils/errores';
import { AlmacenService } from '../../almacenes/services/almacen.service';
import { ZonaService } from '../../zonas/services/zona.service';
import { EstanteriaForm } from '../estanteria-form/estanteria-form';
import { Estanteria } from '../models/estanteria.model';
import { EstanteriaService } from '../services/estanteria.service';

@Component({
  selector: 'app-estanterias-list',
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
  templateUrl: './estanterias-list.html',
  styleUrl: './estanterias-list.scss',
})
export class EstanteriasList {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(EstanteriaService);
  private readonly almacenService = inject(AlmacenService);
  private readonly zonaService = inject(ZonaService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly authService = inject(AuthService);

  readonly esAdmin = this.authService.isAdmin;
  readonly almacenId = Number(this.route.snapshot.paramMap.get('almacenId'));
  readonly zonaId = Number(this.route.snapshot.paramMap.get('zonaId'));

  readonly almacenNombre = signal<string>('');
  readonly zonaNombre = signal<string>('');
  readonly datos = signal<Estanteria[]>([]);
  readonly cargando = signal(false);
  readonly filtroActivo = signal<boolean | null>(null);

  // No hay acción de lectura anidada: la columna de acciones sólo se muestra a ADMIN.
  readonly columnas = computed(() =>
    this.esAdmin()
      ? ['codigo', 'nombre', 'descripcion', 'capacidad', 'estado', 'acciones']
      : ['codigo', 'nombre', 'descripcion', 'capacidad', 'estado'],
  );

  constructor() {
    this.almacenService.obtener(this.almacenId).subscribe({
      next: (a) => this.almacenNombre.set(`${a.codigo} — ${a.nombre}`),
      error: () => this.almacenNombre.set(`Almacén #${this.almacenId}`),
    });
    this.zonaService.obtener(this.zonaId).subscribe({
      next: (z) => this.zonaNombre.set(`${z.codigo} — ${z.nombre}`),
      error: () => this.zonaNombre.set(`Zona #${this.zonaId}`),
    });
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.service.listarPorZona(this.zonaId, this.filtroActivo()).subscribe({
      next: (estanterias) => {
        this.datos.set(estanterias);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  cambiarFiltro(valor: boolean | null): void {
    this.filtroActivo.set(valor);
    this.cargar();
  }

  nuevo(): void {
    this.abrirDialogo(null);
  }

  editar(estanteria: Estanteria): void {
    this.abrirDialogo(estanteria);
  }

  activar(estanteria: Estanteria): void {
    this.service.activar(estanteria.id).subscribe({
      next: () => {
        this.snackBar.open('Estantería activada', 'Cerrar', { duration: 3000 });
        this.cargar();
      },
      error: (err) => this.snackBar.open(mensajeDeError(err), 'Cerrar', { duration: 6000 }),
    });
  }

  desactivar(estanteria: Estanteria): void {
    this.service.desactivar(estanteria.id).subscribe({
      next: () => {
        this.snackBar.open('Estantería desactivada', 'Cerrar', { duration: 3000 });
        this.cargar();
      },
      error: (err) => this.snackBar.open(mensajeDeError(err), 'Cerrar', { duration: 6000 }),
    });
  }

  private abrirDialogo(estanteria: Estanteria | null): void {
    const ref = this.dialog.open(EstanteriaForm, {
      data: { zonaId: this.zonaId, estanteria },
      width: '480px',
    });
    ref.afterClosed().subscribe((guardado: boolean | undefined) => {
      if (guardado) {
        this.snackBar.open(estanteria ? 'Estantería actualizada' : 'Estantería creada', 'Cerrar', {
          duration: 3000,
        });
        this.cargar();
      }
    });
  }
}
