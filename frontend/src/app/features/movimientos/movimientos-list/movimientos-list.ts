import { ComponentType } from '@angular/cdk/overlay';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../core/services/auth.service';
import { formDialogConfig } from '../../../shared/utils/dialog';
import { Producto } from '../../productos/models/producto.model';
import { ProductoService } from '../../productos/services/producto.service';
import { AjusteForm } from '../ajuste-form/ajuste-form';
import { EntradaForm } from '../entrada-form/entrada-form';
import {
  ETIQUETAS_TIPO_MOVIMIENTO,
  MovimientoStock,
  MovimientoTipoHistorial,
  TIPOS_MOVIMIENTO_FILTRO,
} from '../models/movimiento.model';
import { MovimientoService } from '../services/movimiento.service';
import { TrasladoForm } from '../traslado-form/traslado-form';

@Component({
  selector: 'app-movimientos-list',
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
  ],
  templateUrl: './movimientos-list.html',
  styleUrl: './movimientos-list.scss',
})
export class MovimientosList implements OnInit {
  private readonly movimientoService = inject(MovimientoService);
  private readonly productoService = inject(ProductoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly authService = inject(AuthService);

  readonly esAdmin = this.authService.isAdmin;

  readonly datos = signal<MovimientoStock[]>([]);
  readonly total = signal(0);
  readonly cargando = signal(false);
  readonly cargandoFiltros = signal(true);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  readonly filtroProductoId = signal<number | null>(null);
  readonly filtroTipo = signal<MovimientoTipoHistorial | null>(null);
  readonly filtroDesde = signal<string>('');
  readonly filtroHasta = signal<string>('');

  readonly productos = signal<Producto[]>([]);
  readonly tiposMovimiento = TIPOS_MOVIMIENTO_FILTRO;
  readonly etiquetasTipo = ETIQUETAS_TIPO_MOVIMIENTO;

  readonly hayFiltrosActivos = computed(
    () =>
      this.filtroProductoId() !== null ||
      this.filtroTipo() !== null ||
      this.filtroDesde() !== '' ||
      this.filtroHasta() !== '',
  );

  readonly columnas = [
    'fecha',
    'producto',
    'tipo',
    'cantidad',
    'estanteriaOrigen',
    'estanteriaDestino',
    'usuario',
    'motivo',
  ] as const;

  ngOnInit(): void {
    this.productoService.listar(null, null, 0, 500).subscribe({
      next: (page) => {
        this.productos.set(page.content);
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
    this.movimientoService
      .listar(
        {
          productoId: this.filtroProductoId(),
          estanteriaId: null,
          tipo: this.filtroTipo(),
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

  cambiarFiltroProducto(productoId: number | null): void {
    this.filtroProductoId.set(productoId);
    this.pageIndex.set(0);
    this.cargar();
  }

  cambiarFiltroTipo(tipo: MovimientoTipoHistorial | null): void {
    this.filtroTipo.set(tipo);
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

  etiquetaProducto(p: Producto): string {
    return `${p.codigo} — ${p.nombre}`;
  }

  etiquetaTipo(tipo: MovimientoStock['tipo']): string {
    return ETIQUETAS_TIPO_MOVIMIENTO[tipo];
  }

  formatoFecha(fecha: string): string {
    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(fecha));
  }

  formatoCantidad(m: MovimientoStock): string {
    const n = new Intl.NumberFormat('es-AR', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(Math.abs(m.cantidad));

    if (m.tipo === 'AJUSTE') {
      return m.cantidad > 0 ? `+${n}` : `−${n}`;
    }
    if (m.tipo === 'ENTRADA') {
      return `+${n}`;
    }
    return n;
  }

  claseCantidad(m: MovimientoStock): string {
    if (m.tipo === 'AJUSTE') {
      return m.cantidad >= 0 ? 'cantidad-suma' : 'cantidad-resta';
    }
    if (m.tipo === 'ENTRADA') {
      return 'cantidad-suma';
    }
    return 'cantidad-neutral';
  }

  registrarEntrada(): void {
    this.abrirDialogo(EntradaForm, 'Entrada registrada');
  }

  registrarTraslado(): void {
    this.abrirDialogo(TrasladoForm, 'Traslado registrado');
  }

  registrarAjuste(): void {
    this.abrirDialogo(AjusteForm, 'Ajuste registrado');
  }

  private abrirDialogo(component: ComponentType<unknown>, mensajeExito: string): void {
    const ref = this.dialog.open(component, formDialogConfig());
    ref.afterClosed().subscribe((guardado: boolean | undefined) => {
      if (guardado) {
        this.snackBar.open(mensajeExito, 'Cerrar', { duration: 3000 });
        this.cargar();
      }
    });
  }
}
