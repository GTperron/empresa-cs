import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
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
import { startWith } from 'rxjs';
import { SelectorEstanteriaComponent } from '../../../shared/components/selector-estanteria/selector-estanteria';
import { mensajeDeError } from '../../../shared/utils/errores';
import { Producto } from '../../productos/models/producto.model';
import { ProductoService } from '../../productos/services/producto.service';
import { CrearVentaRequest } from '../models/venta.model';
import { VentaService } from '../services/venta.service';

interface LineaFormValue {
  productoId: number | null;
  estanteriaId: number | null;
  cantidad: number | null;
}

@Component({
  selector: 'app-venta-form',
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
  templateUrl: './venta-form.html',
  styleUrl: './venta-form.scss',
})
export class VentaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly ventaService = inject(VentaService);
  private readonly productoService = inject(ProductoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly cargando = signal(false);
  readonly cargandoProductos = signal(true);
  readonly errorMensaje = signal<string | null>(null);

  /** Solo productos tipo VENTA activos. */
  readonly productosVenta = signal<Producto[]>([]);

  readonly form = this.fb.group({
    detalles: this.fb.array([this.crearLinea()]),
  });

  get detalles(): FormArray {
    return this.form.controls.detalles;
  }

  private readonly productosPorId = computed(
    () => new Map(this.productosVenta().map((p) => [p.id, p])),
  );

  /** Snapshot reactivo de las líneas: se actualiza al editar el FormArray. */
  private readonly lineas = toSignal(
    this.detalles.valueChanges.pipe(startWith(this.detalles.getRawValue())),
    { initialValue: this.detalles.getRawValue() as LineaFormValue[] },
  );

  /** Suma de cantidad × precioVenta de cada línea (preview, no se envía). */
  readonly totalEstimado = computed(() =>
    (this.lineas() ?? []).reduce((acc: number, l: LineaFormValue) => acc + this.subtotalEstimado(l), 0),
  );

  ngOnInit(): void {
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
      productoId: this.fb.control<number | null>(null, Validators.required),
      estanteriaId: this.fb.control<number | null>(null, Validators.required),
      cantidad: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.0001)]),
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

  precioReferencia(productoId: number | null): number | null {
    if (productoId == null) {
      return null;
    }
    return this.productosPorId().get(productoId)?.precioVenta ?? null;
  }

  subtotalEstimado(linea: LineaFormValue): number {
    const precio = this.precioReferencia(linea.productoId) ?? 0;
    const cantidad = linea.cantidad ?? 0;
    return precio * cantidad;
  }

  etiquetaProducto(p: Producto): string {
    return `${p.codigo} — ${p.nombre}`;
  }

  formatoMoneda(valor: number): string {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
    }).format(valor);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const request: CrearVentaRequest = {
      detalles: (v.detalles as LineaFormValue[]).map((d) => ({
        productoId: d.productoId!,
        estanteriaId: d.estanteriaId!,
        cantidad: d.cantidad!,
      })),
    };

    this.cargando.set(true);
    this.errorMensaje.set(null);
    this.ventaService.crear(request).subscribe({
      next: (venta) => {
        this.cargando.set(false);
        this.snackBar.open('Venta registrada', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/app/ventas', venta.id]);
      },
      error: (err) => {
        this.cargando.set(false);
        this.errorMensaje.set(mensajeDeError(err));
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/app/ventas']);
  }
}
