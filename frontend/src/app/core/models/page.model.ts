/**
 * Espeja el JSON de un `Page<T>` de Spring Data (dentro de ApiResponse.data).
 * `number` es el índice de página actual (0-based), igual que el pageIndex de mat-paginator.
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
