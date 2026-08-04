import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../../core/services/auth.service';
import { mensajeDeError } from '../../../shared/utils/errores';

/** Valida a nivel de grupo que password y su confirmación coincidan. */
function passwordsCoinciden(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmacion = group.get('passwordConfirmacion')?.value;
  return password === confirmacion ? null : { passwordsNoCoinciden: true };
}

@Component({
  selector: 'app-registro',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './registro.html',
  styleUrl: './registro.scss',
})
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly cargando = signal(false);
  readonly errorMensaje = signal<string | null>(null);
  readonly ocultarPassword = signal(true);

  readonly form = this.fb.nonNullable.group(
    {
      nombre: ['', [Validators.required]],
      apellido: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      passwordConfirmacion: ['', [Validators.required]],
    },
    { validators: passwordsCoinciden },
  );

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.errorMensaje.set(null);

    this.authService.registro(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/app'),
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.errorMensaje.set(mensajeDeError(err, 'No se pudo completar el registro.'));
      },
    });
  }
}
