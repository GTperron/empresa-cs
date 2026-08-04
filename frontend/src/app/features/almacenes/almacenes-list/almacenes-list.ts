import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { mensajeDeError } from '../../../shared/utils/errores';
import { AlmacenForm } from '../almacen-form/almacen-form';
import { Almacen } from '../models/almacen.model';
import { AlmacenService } from '../services/almacen.service';

@Component({
  selector: 'app-almacenes-list',
  imports: [
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTooltipModule,
  ],
  templateUrl: './almacenes-list.html',
  styleUrl: './almacenes-list.scss',
})
export class AlmacenesList {
  private readonly service = inject(AlmacenService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly esAdmin = this.authService.isAdmin;

  readonly datos = signal<Almacen[]>([]);
  readonly total = signal(0);
  readonly cargando = signal(false);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly filtroActivo = signal<boolean | null>(null);

  // La columna de acciones se muestra siempre: "Ver zonas" es de lectura (todos);
  // editar/activar/desactivar quedan condicionados a ADMIN dentro de la celda.
  readonly columnas = computed(() =>
    ['codigo', 'nombre', 'direccion', 'zonas', 'estado', 'acciones'],
  );

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.service.listar(this.filtroActivo(), this.pageIndex(), this.pageSize()).subscribe({
      next: (page) => {
        this.datos.set(page.content);
        this.total.set(page.totalElements);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  cambiarPagina(evento: PageEvent): void {
    this.pageIndex.set(evento.pageIndex);
    this.pageSize.set(evento.pageSize);
    this.cargar();
  }

  cambiarFiltro(valor: boolean | null): void {
    this.filtroActivo.set(valor);
    this.pageIndex.set(0);
    this.cargar();
  }

  verZonas(almacen: Almacen): void {
    this.router.navigate(['/app/almacenes', almacen.id, 'zonas']);
  }

  nuevo(): void {
    this.abrirDialogo(null);
  }

  editar(almacen: Almacen): void {
    this.abrirDialogo(almacen);
  }

  activar(almacen: Almacen): void {
    this.service.activar(almacen.id).subscribe({
      next: () => {
        this.snackBar.open('Almacén activado', 'Cerrar', { duration: 3000 });
        this.cargar();
      },
      error: (err) => this.snackBar.open(mensajeDeError(err), 'Cerrar', { duration: 6000 }),
    });
  }

  desactivar(almacen: Almacen): void {
    this.service.desactivar(almacen.id).subscribe({
      next: () => {
        this.snackBar.open('Almacén desactivado', 'Cerrar', { duration: 3000 });
        this.cargar();
      },
      // 409 "El almacén tiene zonas activas" u otros: mostramos el mensaje específico del backend.
      error: (err) => this.snackBar.open(mensajeDeError(err), 'Cerrar', { duration: 6000 }),
    });
  }

  private abrirDialogo(almacen: Almacen | null): void {
    const ref = this.dialog.open(AlmacenForm, { data: almacen, width: '480px' });
    ref.afterClosed().subscribe((guardado: boolean | undefined) => {
      if (guardado) {
        this.snackBar.open(almacen ? 'Almacén actualizado' : 'Almacén creado', 'Cerrar', { duration: 3000 });
        this.cargar();
      }
    });
  }
}
