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
import {
  ESTADOS_VENTA,
  ETIQUETAS_ESTADO_VENTA,
  Venta,
  VentaEstado,
} from '../models/venta.model';
import { VentaService } from '../services/venta.service';

@Component({
  selector: 'app-ventas-list',
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
  templateUrl: './ventas-list.html',
  styleUrl: './ventas-list.scss',
})
export class VentasList implements OnInit {
  private readonly ventaService = inject(VentaService);
  private readonly router = inject(Router);

  readonly datos = signal<Venta[]>([]);
  readonly total = signal(0);
  readonly cargando = signal(false);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  readonly filtroEstado = signal<VentaEstado | null>(null);
  readonly filtroDesde = signal<string>('');
  readonly filtroHasta = signal<string>('');

  readonly estados = ESTADOS_VENTA;
  readonly etiquetasEstado = ETIQUETAS_ESTADO_VENTA;

  readonly hayFiltrosActivos = computed(
    () => this.filtroEstado() !== null || this.filtroDesde() !== '' || this.filtroHasta() !== '',
  );

  readonly columnas = ['fecha', 'total', 'estado', 'lineas', 'usuario', 'acciones'] as const;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.ventaService
      .listar(
        {
          estado: this.filtroEstado(),
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

  cambiarFiltroEstado(estado: VentaEstado | null): void {
    this.filtroEstado.set(estado);
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
    this.router.navigate(['/app/ventas/nueva']);
  }

  verDetalle(v: Venta): void {
    this.router.navigate(['/app/ventas', v.id]);
  }

  etiquetaEstado(estado: VentaEstado): string {
    return ETIQUETAS_ESTADO_VENTA[estado];
  }

  formatoFecha(fecha: string): string {
    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(fecha));
  }

  formatoMoneda(valor: number): string {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
    }).format(valor);
  }
}
