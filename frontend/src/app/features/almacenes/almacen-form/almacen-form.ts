import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { mensajeDeError } from '../../../shared/utils/errores';
import { Almacen } from '../models/almacen.model';
import { AlmacenService } from '../services/almacen.service';

/**
 * Diálogo reutilizable para crear (data = null) o editar (data = almacén) un almacén.
 * Devuelve `true` al cerrarse si se guardó, para que el listado recargue.
 */
@Component({
  selector: 'app-almacen-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './almacen-form.html',
  styleUrl: './almacen-form.scss',
})
export class AlmacenForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AlmacenService);
  private readonly dialogRef = inject(MatDialogRef<AlmacenForm, boolean>);
  protected readonly data = inject<Almacen | null>(MAT_DIALOG_DATA);

  readonly esEdicion = this.data !== null;
  readonly cargando = signal(false);
  readonly errorMensaje = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    codigo: [this.data?.codigo ?? '', [Validators.required, Validators.maxLength(50)]],
    nombre: [this.data?.nombre ?? '', [Validators.required, Validators.maxLength(100)]],
    direccion: [this.data?.direccion ?? '', [Validators.maxLength(255)]],
  });

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
      direccion: valor.direccion.trim() || undefined,
    };

    const operacion = this.esEdicion
      ? this.service.editar(this.data!.id, request)
      : this.service.crear(request);

    operacion.subscribe({
      next: () => this.dialogRef.close(true),
      error: (err) => {
        this.cargando.set(false);
        this.errorMensaje.set(mensajeDeError(err, 'No se pudo guardar el almacén.'));
      },
    });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
