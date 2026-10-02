import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { mensajeDeError } from '../../../shared/utils/errores';
import { ETIQUETAS_ESTADO_VENTA, Venta, VentaEstado } from '../models/venta.model';
import { VentaService } from '../services/venta.service';

@Component({
  selector: 'app-venta-detalle',
  imports: [MatButtonModule, MatIconModule, MatProgressBarModule, MatTableModule, MatTooltipModule],
  templateUrl: './venta-detalle.html',
  styleUrl: './venta-detalle.scss',
})
export class VentaDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly ventaService = inject(VentaService);

  readonly cargando = signal(true);
  readonly errorMensaje = signal<string | null>(null);
  readonly venta = signal<Venta | null>(null);

  readonly columnas = [
    'producto',
    'estanteria',
    'cantidad',
    'precioUnitario',
    'subtotal',
  ] as const;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMensaje.set('Venta no válida.');
      this.cargando.set(false);
      return;
    }

    this.ventaService.obtener(id).subscribe({
      next: (v) => {
        this.venta.set(v);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMensaje.set(mensajeDeError(err, 'No se pudo cargar la venta.'));
        this.cargando.set(false);
      },
    });
  }

  volver(): void {
    this.router.navigate(['/app/ventas']);
  }

  etiquetaEstado(estado: VentaEstado): string {
    return ETIQUETAS_ESTADO_VENTA[estado];
  }

  formatoFecha(fecha: string): string {
    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'long',
      timeStyle: 'short',
    }).format(new Date(fecha));
  }

  formatoCantidad(valor: number): string {
    return new Intl.NumberFormat('es-AR', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(valor);
  }

  formatoMoneda(valor: number): string {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
    }).format(valor);
  }
}
