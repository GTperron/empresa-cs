import { Component, inject, signal } from '@angular/core';
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
import { AuthService } from '../../../core/services/auth.service';
import { formDialogConfig } from '../../../shared/utils/dialog';
import { mensajeDeError } from '../../../shared/utils/errores';
import {
  ETIQUETAS_TIPO,
  ETIQUETAS_UNIDAD,
  Producto,
  ProductoTipo,
} from '../models/producto.model';
import { ProductoForm } from '../producto-form/producto-form';
import { ProductoService } from '../services/producto.service';

@Component({
  selector: 'app-productos-list',
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
  templateUrl: './productos-list.html',
  styleUrl: './productos-list.scss',
})
export class ProductosList {
  private readonly service = inject(ProductoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly authService = inject(AuthService);

  readonly esAdmin = this.authService.isAdmin;

  readonly datos = signal<Producto[]>([]);
  readonly total = signal(0);
  readonly cargando = signal(false);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly filtroTipo = signal<ProductoTipo | null>(null);
  readonly filtroActivo = signal<boolean | null>(null);

  readonly columnas = [
    'codigo',
    'nombre',
    'tipo',
    'unidadMedida',
    'precioVenta',
    'estado',
    'acciones',
  ] as const;

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.service
      .listar(this.filtroTipo(), this.filtroActivo(), this.pageIndex(), this.pageSize())
      .subscribe({
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

  cambiarFiltroTipo(valor: ProductoTipo | null): void {
    this.filtroTipo.set(valor);
    this.pageIndex.set(0);
    this.cargar();
  }

  cambiarFiltroActivo(valor: boolean | null): void {
    this.filtroActivo.set(valor);
    this.pageIndex.set(0);
    this.cargar();
  }

  formatoPrecio(producto: Producto): string {
    if (producto.tipo !== 'VENTA' || producto.precioVenta == null) {
      return '—';
    }
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      minimumFractionDigits: 2,
    }).format(producto.precioVenta);
  }

  etiquetaTipo(tipo: ProductoTipo): string {
    return ETIQUETAS_TIPO[tipo];
  }

  etiquetaUnidad(unidad: Producto['unidadMedida']): string {
    return ETIQUETAS_UNIDAD[unidad];
  }

  nuevo(): void {
    this.abrirDialogo(null);
  }

  editar(producto: Producto): void {
    this.abrirDialogo(producto);
  }

  activar(producto: Producto): void {
    this.service.activar(producto.id).subscribe({
      next: () => {
        this.snackBar.open('Producto activado', 'Cerrar', { duration: 3000 });
        this.cargar();
      },
      error: (err) => this.snackBar.open(mensajeDeError(err), 'Cerrar', { duration: 6000 }),
    });
  }

  desactivar(producto: Producto): void {
    this.service.desactivar(producto.id).subscribe({
      next: () => {
        this.snackBar.open('Producto desactivado', 'Cerrar', { duration: 3000 });
        this.cargar();
      },
      error: (err) => this.snackBar.open(mensajeDeError(err), 'Cerrar', { duration: 6000 }),
    });
  }

  private abrirDialogo(producto: Producto | null): void {
    const ref = this.dialog.open(ProductoForm, formDialogConfig({ data: producto }));
    ref.afterClosed().subscribe((guardado: boolean | undefined) => {
      if (guardado) {
        this.snackBar.open(
          producto ? 'Producto actualizado' : 'Producto creado',
          'Cerrar',
          { duration: 3000 },
        );
        this.cargar();
      }
    });
  }
}
