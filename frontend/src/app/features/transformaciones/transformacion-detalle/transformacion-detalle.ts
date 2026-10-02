import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { mensajeDeError } from '../../../shared/utils/errores';
import { Transformacion } from '../models/transformacion.model';
import { TransformacionService } from '../services/transformacion.service';

@Component({
  selector: 'app-transformacion-detalle',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './transformacion-detalle.html',
  styleUrl: './transformacion-detalle.scss',
})
export class TransformacionDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly transformacionService = inject(TransformacionService);

  readonly cargando = signal(true);
  readonly errorMensaje = signal<string | null>(null);
  readonly transformacion = signal<Transformacion | null>(null);

  readonly columnas = ['productoVenta', 'estanteriaDestino', 'cantidadGenerada'] as const;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMensaje.set('Transformación no válida.');
      this.cargando.set(false);
      return;
    }

    this.transformacionService.obtener(id).subscribe({
      next: (t) => {
        this.transformacion.set(t);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMensaje.set(mensajeDeError(err, 'No se pudo cargar la transformación.'));
        this.cargando.set(false);
      },
    });
  }

  volver(): void {
    this.router.navigate(['/app/transformaciones']);
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
}
