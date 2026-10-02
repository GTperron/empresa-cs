import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { Producto } from '../../productos/models/producto.model';
import { ProductoService } from '../../productos/services/producto.service';
import { Transformacion } from '../models/transformacion.model';
import { TransformacionService } from '../services/transformacion.service';

@Component({
  selector: 'app-transformaciones-list',
  imports: [
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatTooltipModule,
  ],
  templateUrl: './transformaciones-list.html',
  styleUrl: './transformaciones-list.scss',
})
export class TransformacionesList implements OnInit {
  private readonly transformacionService = inject(TransformacionService);
  private readonly productoService = inject(ProductoService);
  private readonly router = inject(Router);

  readonly datos = signal<Transformacion[]>([]);
  readonly total = signal(0);
  readonly cargando = signal(false);
  readonly cargandoFiltros = signal(true);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  readonly filtroProductoEntradaId = signal<number | null>(null);
  readonly filtroDesde = signal<string>('');
  readonly filtroHasta = signal<string>('');

  /** Solo insumos (tipo ENTRADA) para el filtro por producto consumido. */
  readonly productosEntrada = signal<Producto[]>([]);

  readonly hayFiltrosActivos = computed(
    () =>
      this.filtroProductoEntradaId() !== null ||
      this.filtroDesde() !== '' ||
      this.filtroHasta() !== '',
  );

  readonly columnas = [
    'fecha',
    'productoEntrada',
    'cantidadConsumida',
    'lineas',
    'usuario',
    'acciones',
  ] as const;

  ngOnInit(): void {
    this.productoService.listar('ENTRADA', null, 0, 500).subscribe({
      next: (page) => {
        this.productosEntrada.set(page.content);
        this.cargandoFiltros.set(false);
        this.cargar();
      },
      error: () => {
        this.cargandoFiltros.set(false);
        this.cargar();
      },
    });
  }

  cargar(): void {
    this.cargando.set(true);
    this.transformacionService
      .listar(
        {
          productoEntradaId: this.filtroProductoEntradaId(),
          desde: this.filtroDesde() ? `${this.filtroDesde()}T00:00:00` : null,
          hasta: this.filtroHasta() ? `${this.filtroHasta()}T23:59:59` : null,
        },
        this.pageIndex(),
        this.pageSize(),
      )
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

  cambiarFiltroProducto(productoEntradaId: number | null): void {
    this.filtroProductoEntradaId.set(productoEntradaId);
    this.pageIndex.set(0);
    this.cargar();
  }

  cambiarFiltroDesde(valor: string): void {
    this.filtroDesde.set(valor);
    this.pageIndex.set(0);
    this.cargar();
  }

  cambiarFiltroHasta(valor: string): void {
    this.filtroHasta.set(valor);
    this.pageIndex.set(0);
    this.cargar();
  }

  nueva(): void {
    this.router.navigate(['/app/transformaciones/nueva']);
  }

  verDetalle(t: Transformacion): void {
    this.router.navigate(['/app/transformaciones', t.id]);
  }

  etiquetaProducto(p: Producto): string {
    return `${p.codigo} — ${p.nombre}`;
  }

  formatoFecha(fecha: string): string {
    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(fecha));
  }

  formatoCantidad(valor: number): string {
    return new Intl.NumberFormat('es-AR', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(valor);
  }
}
