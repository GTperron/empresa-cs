import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { mensajeDeError } from '../../../shared/utils/errores';
import { Estanteria } from '../models/estanteria.model';
import { EstanteriaService } from '../services/estanteria.service';

/** Datos del diálogo: la zona padre (para crear) y la estantería (null = alta). */
export interface EstanteriaFormData {
  zonaId: number;
  estanteria: Estanteria | null;
}

/**
 * Diálogo para crear/editar una estantería. Igual patrón que los demás,
 * con el campo extra opcional `capacidadMaxima` (entero >= 0).
 */
@Component({
  selector: 'app-estanteria-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './estanteria-form.html',
  styleUrl: './estanteria-form.scss',
})
export class EstanteriaForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(EstanteriaService);
  private readonly dialogRef = inject(MatDialogRef<EstanteriaForm, boolean>);
  protected readonly data = inject<EstanteriaFormData>(MAT_DIALOG_DATA);

  readonly esEdicion = this.data.estanteria !== null;
  readonly cargando = signal(false);
  readonly errorMensaje = signal<string | null>(null);

  readonly form = this.fb.group({
    codigo: this.fb.nonNullable.control(this.data.estanteria?.codigo ?? '', [
      Validators.required,
      Validators.maxLength(50),
    ]),
    nombre: this.fb.nonNullable.control(this.data.estanteria?.nombre ?? '', [
      Validators.required,
      Validators.maxLength(100),
    ]),
    descripcion: this.fb.nonNullable.control(this.data.estanteria?.descripcion ?? '', [
      Validators.maxLength(255),
    ]),
    // Puede quedar vacía (null); si se completa, debe ser entero >= 0.
    capacidadMaxima: this.fb.control<number | null>(this.data.estanteria?.capacidadMaxima ?? null, [
      Validators.min(0),
    ]),
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
      capacidadMaxima: valor.capacidadMaxima ?? undefined,
    };

    const operacion = this.esEdicion
      ? this.service.editar(this.data.estanteria!.id, request)
      : this.service.crear(this.data.zonaId, request);

    operacion.subscribe({
      next: () => this.dialogRef.close(true),
      error: (err) => {
        this.cargando.set(false);
        this.errorMensaje.set(mensajeDeError(err, 'No se pudo guardar la estantería.'));
      },
    });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }
}
