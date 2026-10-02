import { inject, Injectable } from '@angular/core';
import { forkJoin, map, Observable, of, shareReplay, switchMap } from 'rxjs';
import { AlmacenService } from '../../almacenes/services/almacen.service';
import { EstanteriaService } from '../../estanterias/services/estanteria.service';
import { ZonaService } from '../../zonas/services/zona.service';
import { EstanteriaGrupo, EstanteriaOpcion } from '../models/estanteria-catalogo.model';

/**
 * Árbol Almacén → Zona → Estantería (solo activos) con cache en memoria
 * para reutilizar en SelectorEstanteriaComponent y filtros.
 */
@Injectable({ providedIn: 'root' })
export class EstanteriaCatalogoService {
  private readonly almacenService = inject(AlmacenService);
  private readonly zonaService = inject(ZonaService);
  private readonly estanteriaService = inject(EstanteriaService);

  private cache$: Observable<EstanteriaGrupo[]> | null = null;

  obtenerGrupos(): Observable<EstanteriaGrupo[]> {
    if (!this.cache$) {
      this.cache$ = this.cargarArbol().pipe(shareReplay(1));
    }
    return this.cache$;
  }

  invalidarCache(): void {
    this.cache$ = null;
  }

  private cargarArbol(): Observable<EstanteriaGrupo[]> {
    return this.almacenService.listar(true, 0, 500).pipe(
      switchMap((page) => {
        const almacenes = page.content;
        if (almacenes.length === 0) {
          return of([] as EstanteriaGrupo[]);
        }

        return forkJoin(
          almacenes.map((almacen) =>
            this.zonaService.listarPorAlmacen(almacen.id, true).pipe(
              switchMap((zonas) => {
                if (zonas.length === 0) {
                  return of({ almacen, filas: [] as EstanteriaOpcion[] });
                }
                return forkJoin(
                  zonas.map((zona) =>
                    this.estanteriaService.listarPorZona(zona.id, true).pipe(
                      map((estanterias) =>
                        estanterias.map(
                          (e): EstanteriaOpcion => ({
                            estanteriaId: e.id,
                            almacenId: almacen.id,
                            zonaId: zona.id,
                            label: `${almacen.codigo} › ${zona.codigo} › ${e.codigo} — ${e.nombre}`,
                          }),
                        ),
                      ),
                    ),
                  ),
                ).pipe(
                  map((porZona) => ({
                    almacen,
                    filas: porZona.flat(),
                  })),
                );
              }),
            ),
          ),
        ).pipe(
          map((resultados) =>
            resultados
              .filter((r) => r.filas.length > 0)
              .map(
                (r): EstanteriaGrupo => ({
                  almacenId: r.almacen.id,
                  almacenLabel: `${r.almacen.codigo} — ${r.almacen.nombre}`,
                  opciones: r.filas.sort((a, b) => a.label.localeCompare(b.label, 'es')),
                }),
              )
              .sort((a, b) => a.almacenLabel.localeCompare(b.almacenLabel, 'es')),
          ),
        );
      }),
    );
  }
}
