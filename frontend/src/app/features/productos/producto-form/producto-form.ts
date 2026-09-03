import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { mensajeDeError } from '../../../shared/utils/errores';
import {
  ETIQUETAS_TIPO,
  ETIQUETAS_UNIDAD,
  Producto,
  ProductoTipo,
  TIPOS_PRODUCTO,
  UNIDADES_MEDIDA,
} from '../models/producto.model';
import { ProductoService } from '../services/producto.service';

/**
 * Diálogo para crear (data = null) o editar (data = producto) un producto.
 * precioVenta sólo aplica y se envía cuando tipo === VENTA.
 */
@Component({
  selector: 'app-producto-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './producto-form.html',
  styleUrl: './producto-form.scss',
})
export class ProductoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProductoService);
  private readonly dialogRef = inject(MatDialogRef<ProductoForm, boolean>);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly data = inject<Producto | null>(MAT_DIALOG_DATA);

  readonly esEdicion = this.data !== null;
  readonly cargando = signal(false);
  readonly errorMensaje = signal<string | null>(null);
  readonly esTipoVenta = signal((this.data?.tipo ?? 'ENTRADA') === 'VENTA');

  readonly tiposProducto = TIPOS_PRODUCTO;
  readonly unidadesMedida = UNIDADES_MEDIDA;
  readonly etiquetasTipo = ETIQUETAS_TIPO;
  readonly etiquetasUnidad = ETIQUETAS_UNIDAD;

  readonly form = this.fb.nonNullable.group({
    codigo: [this.data?.codigo ?? '', [Validators.required, Validators.maxLength(50)]],
    nombre: [this.data?.nombre ?? '', [Validators.required, Validators.maxLength(100)]],
    descripcion: [this.data?.descripcion ?? '', [Validators.maxLength(255)]],
    tipo: [this.data?.tipo ?? ('ENTRADA' as ProductoTipo), Validators.required],
    unidadMedida: [this.data?.unidadMedida ?? 'UNIDAD', Validators.required],
    precioVenta: this.fb.control<number | null>(this.data?.precioVenta ?? null),
  });

  ngOnInit(): void {
    this.aplicarReglasPrecio(this.form.controls.tipo.value);

    this.form.controls.tipo.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((tipo) => this.aplicarReglasPrecio(tipo));
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.errorMensaje.set(null);

    const valor = this.form.getRawValue();
    const request = {
      codigo: valor.codigo.trim(),
      nombre: valor.nombre.trim(),
      descripcion: valor.descripcion.trim() || undefined,
      tipo: valor.tipo,
      unidadMedida: valor.unidadMedida,
      ...(valor.tipo === 'VENTA' && valor.precioVenta !== null
        ? { precioVenta: valor.precioVenta }
        : {}),
    };

    const operacion = this.esEdicion
      ? this.service.editar(this.data!.id, request)
      : this.service.crear(request);

    operacion.subscribe({
      next: () => this.dialogRef.close(true),
      error: (err) => {
        this.cargando.set(false);
        this.errorMensaje.set(mensajeDeError(err, 'No se pudo guardar el producto.'));
      },
    });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }

  private aplicarReglasPrecio(tipo: ProductoTipo): void {
    const precio = this.form.controls.precioVenta;
    this.esTipoVenta.set(tipo === 'VENTA');

    if (tipo === 'VENTA') {
      precio.enable({ emitEvent: false });
      precio.setValidators([Validators.required, Validators.min(0)]);
    } else {
      precio.setValue(null, { emitEvent: false });
      precio.clearValidators();
      precio.disable({ emitEvent: false });
    }
    precio.updateValueAndValidity({ emitEvent: false });
  }
}
