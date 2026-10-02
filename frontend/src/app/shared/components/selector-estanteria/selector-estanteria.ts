import { Component, forwardRef, inject, input, OnInit, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { EstanteriaGrupo } from '../../../features/movimientos/models/estanteria-catalogo.model';
import { EstanteriaCatalogoService } from '../../../features/movimientos/services/estanteria-catalogo.service';

/**
 * mat-select único agrupado por almacén (opción B).
 * Implementa ControlValueAccessor para formControlName / ngModel.
 */
@Component({
  selector: 'app-selector-estanteria',
  imports: [MatFormFieldModule, MatSelectModule, MatProgressBarModule],
  templateUrl: './selector-estanteria.html',
  styleUrl: './selector-estanteria.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectorEstanteriaComponent),
      multi: true,
    },
  ],
})
export class SelectorEstanteriaComponent implements ControlValueAccessor, OnInit {
  private readonly catalogo = inject(EstanteriaCatalogoService);

  readonly label = input('Estantería');
  readonly excluirIds = input<number[]>([]);

  readonly grupos = signal<EstanteriaGrupo[]>([]);
  readonly cargando = signal(true);
  readonly errorCarga = signal(false);

  value: number | null = null;
  disabled = false;

  private onChange: (value: number | null) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  ngOnInit(): void {
    this.catalogo.obtenerGrupos().subscribe({
      next: (grupos) => {
        this.grupos.set(grupos);
        this.cargando.set(false);
      },
      error: () => {
        this.errorCarga.set(true);
        this.cargando.set(false);
      },
    });
  }

  writeValue(value: number | null): void {
    this.value = value;
  }

  registerOnChange(fn: (value: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  seleccionar(estanteriaId: number | null): void {
    this.value = estanteriaId;
    this.onChange(estanteriaId);
    this.onTouched();
  }

  estaExcluida(estanteriaId: number): boolean {
    return this.excluirIds().includes(estanteriaId);
  }
}
