import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { mensajeDeError } from '../../../shared/utils/errores';
import { Zona } from '../models/zona.model';
import { ZonaService } from '../services/zona.service';

/** Datos que recibe el diálogo: el almacén padre (para crear) y la zona (null = alta). */
export interface ZonaFormData {
  almacenId: number;
  zona: Zona | null;
}

/**
 * Diálogo para crear/editar una zona. Mismo patrón que AlmacenForm.
 * Devuelve `true` al cerrarse si se guardó.
 */
@Component({
  selector: 'app-zona-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './zona-form.html',
  styleUrl: './zona-form.scss',
})
export class ZonaForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ZonaService);
  private readonly dialogRef = inject(MatDialogRef<ZonaForm, boolean>);
  protected readonly data = inject<ZonaFormData>(MAT_DIALOG_DATA);

  readonly esEdicion = this.data.zona !== null;
  readonly cargando = signal(false);
  readonly errorMensaje = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    codigo: [this.data.zona?.codigo ?? '', [Validators.required, Validators.maxLength(50)]],
    nombre: [this.data.zona?.nombre ?? '', [Validators.required, Validators.maxLength(100)]],
    descripcion: [this.data.zona?.descripcion ?? '', [Validators.maxLength(255)]],
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
      descripcion: valor.descripcion.trim() || undefined,
    };

    const operacion = this.esEdicion
      ? this.service.editar(this.data.zona!.id, request)
      : this.service.crear(this.data.almacenId, request);

    operacion.subscribe({
      next: () => this.dialogRef.close(true),
      error: (err) => {
        this.cargando.set(false);
        this.errorMensaje.set(mensajeDeError(err, 'No se pudo guardar la zona.'));
      },
    });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
