import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { catchError, forkJoin, of } from 'rxjs';
import { Page } from '../../../core/models';
import { Almacen } from '../../almacenes/models/almacen.model';
import { AlmacenService } from '../../almacenes/services/almacen.service';
import { ETIQUETAS_UNIDAD, Producto, UnidadMedida } from '../../productos/models/producto.model';
import { ProductoService } from '../../productos/services/producto.service';
import { Stock } from '../models/stock.model';
import { StockService } from '../services/stock.service';

@Component({
  selector: 'app-stock-list',
  imports: [
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
  ],
  templateUrl: './stock-list.html',
  styleUrl: './stock-list.scss',
})
export class StockList {
  private readonly stockService = inject(StockService);
  private readonly productoService = inject(ProductoService);
  private readonly almacenService = inject(AlmacenService);

  readonly datos = signal<Stock[]>([]);
  readonly total = signal(0);
  readonly cargando = signal(false);
  readonly cargandoFiltros = signal(true);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly filtroProductoId = signal<number | null>(null);
  readonly filtroAlmacenId = signal<number | null>(null);

  readonly productos = signal<Producto[]>([]);
  readonly almacenes = signal<Almacen[]>([]);
  private readonly unidadPorProductoId = signal<Map<number, UnidadMedida>>(new Map());

  readonly hayFiltrosActivos = computed(
    () => this.filtroProductoId() !== null || this.filtroAlmacenId() !== null,
  );

  readonly columnas = ['producto', 'ubicacion', 'cantidad', 'unidadMedida'] as const;

  /** Módulo Movimientos (Próximamente) — destino para cargar stock. */
  readonly rutaMovimientos = '/app/proximamente/movimientos';

  constructor() {
    this.cargarOpcionesFiltro();
  }

  cargarOpcionesFiltro(): void {
    this.cargandoFiltros.set(true);
    forkJoin({
      productos: this.productoService.listar(null, null, 0, 500).pipe(
        catchError(() => of(paginaVacia<Producto>())),
      ),
      // null = todos los almacenes (activos e inactivos), igual que filtro "Todos" en Almacenes
      almacenes: this.almacenService.listar(null, 0, 500).pipe(
        catchError(() => of(paginaVacia<Almacen>())),
      ),
    }).subscribe({
      next: ({ productos, almacenes }) => {
        this.productos.set(productos.content);
        this.almacenes.set(almacenes.content);
        this.unidadPorProductoId.set(
          new Map(productos.content.map((p) => [p.id, p.unidadMedida])),
        );
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
    this.stockService
      .listar(
        {
          productoId: this.filtroProductoId(),
          estanteriaId: null,
          almacenId: this.filtroAlmacenId(),
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

  cambiarFiltroProducto(productoId: number | null): void {
    this.filtroProductoId.set(productoId);
    this.pageIndex.set(0);
    this.cargar();
  }

  cambiarFiltroAlmacen(almacenId: number | null): void {
    this.filtroAlmacenId.set(almacenId);
    this.pageIndex.set(0);
    this.cargar();
  }

  etiquetaProducto(p: Producto): string {
    return `${p.codigo} — ${p.nombre}`;
  }

  etiquetaAlmacen(a: Almacen): string {
    return `${a.codigo} — ${a.nombre}`;
  }

  textoUbicacion(stock: Stock): string {
    return `${stock.almacenCodigo} > ${stock.zonaCodigo} > ${stock.estanteriaCodigo}`;
  }

  etiquetaUnidad(stock: Stock): string {
    const unidad = this.unidadPorProductoId().get(stock.productoId);
    return unidad ? ETIQUETAS_UNIDAD[unidad] : '—';
  }

  formatoCantidad(cantidad: number): string {
    return new Intl.NumberFormat('es-AR', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(cantidad);
  }
}

function paginaVacia<T>(): Page<T> {
  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 0,
    first: true,
    last: true,
    numberOfElements: 0,
    empty: true,
  };
}
