/** Opción plana para mat-select (selector único agrupado por almacén). */
export interface EstanteriaOpcion {
  estanteriaId: number;
  almacenId: number;
  zonaId: number;
  /** Ej.: DEP-01 › Z-A › E-01 — Pasillo A */
  label: string;
}

/** Grupo mat-optgroup por almacén. */
export interface EstanteriaGrupo {
  almacenId: number;
  /** Ej.: DEP-01 — Depósito Central */
  almacenLabel: string;
  opciones: EstanteriaOpcion[];
}
