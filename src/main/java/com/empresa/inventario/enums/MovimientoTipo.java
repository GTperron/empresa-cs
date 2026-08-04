package com.empresa.inventario.enums;

/**
 * Tipo de movimiento de stock.
 * TRANSFORMACION: consumo de insumo / generación de producto de venta.
 * SALIDA_VENTA: descuento de stock por una venta.
 */
public enum MovimientoTipo {
    ENTRADA,
    TRASLADO,
    AJUSTE,
    TRANSFORMACION,
    SALIDA_VENTA
}
