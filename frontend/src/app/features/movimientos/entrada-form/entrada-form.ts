import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { SelectorEstanteriaComponent } from '../../../shared/components/selector-estanteria/selector-estanteria';
import { mensajeDeError } from '../../../shared/utils/errores';
import { Producto } from '../../productos/models/producto.model';
import { ProductoService } from '../../productos/services/producto.service';
import { MovimientoService } from '../services/movimiento.service';

@Component({
  selector: 'app-entrada-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressBarModule,
    SelectorEstanteriaComponent,
  ],
  templateUrl: './entrada-form.html',
  styleUrl: './entrada-form.scss',
})
export class EntradaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly movimientoService = inject(MovimientoService);
  private readonly productoService = inject(ProductoService);
  private readonly dialogRef = inject(MatDialogRef<EntradaForm, boolean>);

  readonly cargando = signal(false);
  readonly cargandoProductos = signal(true);
  readonly errorMensaje = signal<string | null>(null);
  readonly productos = signal<Producto[]>([]);

  readonly form = this.fb.nonNullable.group({
    productoId: this.fb.control<number | null>(null, Validators.required),
    estanteriaId: this.fb.control<number | null>(null, Validators.required),
    cantidad: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.0001)]),
  });

  ngOnInit(): void {
    this.productoService.listar('ENTRADA', true, 0, 500).subscribe({
      next: (page) => {
        this.productos.set(page.content);
        this.cargandoProductos.set(false);
      },
      error: () => {
        this.productos.set([]);
        this.cargandoProductos.set(false);
      },
    });
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
    this.cargando.set(true);
    this.errorMensaje.set(null);

    this.movimientoService
      .registrarEntrada({
        productoId: v.productoId!,
        estanteriaId: v.estanteriaId!,
        cantidad: v.cantidad!,
      })
      .subscribe({
        next: () => this.dialogRef.close(true),
        error: (err) => {
          this.cargando.set(false);
          this.errorMensaje.set(mensajeDeError(err, 'No se pudo registrar la entrada.'));
        },
      });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
