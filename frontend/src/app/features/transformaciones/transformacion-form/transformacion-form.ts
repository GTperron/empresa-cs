import { Component, inject, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { SelectorEstanteriaComponent } from '../../../shared/components/selector-estanteria/selector-estanteria';
import { mensajeDeError } from '../../../shared/utils/errores';
import { Producto } from '../../productos/models/producto.model';
import { ProductoService } from '../../productos/services/producto.service';
import { CrearTransformacionRequest } from '../models/transformacion.model';
import { TransformacionService } from '../services/transformacion.service';

@Component({
  selector: 'app-transformacion-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatTooltipModule,
    SelectorEstanteriaComponent,
  ],
  templateUrl: './transformacion-form.html',
  styleUrl: './transformacion-form.scss',
})
export class TransformacionForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly transformacionService = inject(TransformacionService);
  private readonly productoService = inject(ProductoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly cargando = signal(false);
  readonly cargandoProductos = signal(true);
  readonly errorMensaje = signal<string | null>(null);

  /** Insumos: solo tipo ENTRADA activos. */
  readonly productosEntrada = signal<Producto[]>([]);
  /** Generados: solo tipo VENTA activos (compartidos entre todas las líneas). */
  readonly productosVenta = signal<Producto[]>([]);

  readonly form = this.fb.group({
    productoEntradaId: this.fb.control<number | null>(null, Validators.required),
    estanteriaOrigenId: this.fb.control<number | null>(null, Validators.required),
    cantidadConsumida: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(0.0001),
    ]),
    observaciones: this.fb.control<string>('', Validators.maxLength(1000)),
    detalles: this.fb.array([this.crearLinea()]),
  });

  get detalles(): FormArray {
    return this.form.controls.detalles;
  }

  ngOnInit(): void {
    this.productoService.listar('ENTRADA', true, 0, 500).subscribe({
      next: (page) => this.productosEntrada.set(page.content),
      error: () => this.productosEntrada.set([]),
    });

    this.productoService.listar('VENTA', true, 0, 500).subscribe({
      next: (page) => {
        this.productosVenta.set(page.content);
        this.cargandoProductos.set(false);
      },
      error: () => {
        this.productosVenta.set([]);
        this.cargandoProductos.set(false);
      },
    });
  }

  private crearLinea() {
    return this.fb.group({
      productoVentaId: this.fb.control<number | null>(null, Validators.required),
      estanteriaDestinoId: this.fb.control<number | null>(null, Validators.required),
      cantidadGenerada: this.fb.control<number | null>(null, [
        Validators.required,
        Validators.min(0.0001),
      ]),
    });
  }

  agregarLinea(): void {
    this.detalles.push(this.crearLinea());
  }

  quitarLinea(indice: number): void {
    if (this.detalles.length > 1) {
      this.detalles.removeAt(indice);
    }
  }

  etiquetaProducto(p: Producto): string {
    return `${p.codigo} — ${p.nombre}`;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const request: CrearTransformacionRequest = {
      productoEntradaId: v.productoEntradaId!,
      estanteriaOrigenId: v.estanteriaOrigenId!,
      cantidadConsumida: v.cantidadConsumida!,
      observaciones: v.observaciones?.trim() ? v.observaciones.trim() : undefined,
      detalles: (v.detalles as Array<{
        productoVentaId: number;
        estanteriaDestinoId: number;
        cantidadGenerada: number;
      }>).map((d) => ({
        productoVentaId: d.productoVentaId,
        estanteriaDestinoId: d.estanteriaDestinoId,
        cantidadGenerada: d.cantidadGenerada,
      })),
    };

    this.cargando.set(true);
    this.errorMensaje.set(null);
    this.transformacionService.crear(request).subscribe({
      next: (transformacion) => {
        this.cargando.set(false);
        this.snackBar.open('Transformación registrada', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/app/transformaciones', transformacion.id]);
      },
      error: (err) => {
        this.cargando.set(false);
        this.errorMensaje.set(mensajeDeError(err));
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/app/transformaciones']);
  }
}
