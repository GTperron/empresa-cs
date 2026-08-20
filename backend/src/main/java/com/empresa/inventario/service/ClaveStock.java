package com.empresa.inventario.service;

/**
 * Clave que identifica una fila de stock (producto + estantería).
 * Ordenable determinísticamente (primero por estantería, luego por producto)
 * para bloquear múltiples filas en un orden estable y evitar deadlocks.
 */
public record ClaveStock(Long productoId, Long estanteriaId) implements Comparable<ClaveStock> {

    @Override
    public int compareTo(ClaveStock otra) {
        int cmp = Long.compare(this.estanteriaId, otra.estanteriaId);
        if (cmp != 0) {
            return cmp;
        }
        return Long.compare(this.productoId, otra.productoId);
    }
}
