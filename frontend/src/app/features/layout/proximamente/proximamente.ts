import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { map } from 'rxjs';

@Component({
  selector: 'app-proximamente',
  imports: [MatIconModule],
  templateUrl: './proximamente.html',
  styleUrl: './proximamente.scss',
})
export class Proximamente {
  private readonly route = inject(ActivatedRoute);

  // Se lee del paramMap (observable) porque el componente se reutiliza al navegar
  // entre módulos y el snapshot no se actualizaría en ese caso.
  private readonly modulo = toSignal(
    this.route.paramMap.pipe(map((p) => p.get('modulo'))),
    { initialValue: null },
  );

  readonly titulo = computed(() => {
    const m = this.modulo();
    return m ? m.charAt(0).toUpperCase() + m.slice(1) : null;
  });
}
