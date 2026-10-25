package com.apuntia.app.domain.model

/**
 * Forma que acompaña al color de una categoría. Sirve para distinguirlas sin depender solo del
 * color, por ejemplo para personas con daltonismo.
 */
enum class CategoryShape {
    /** Círculo (Escuela). */
    CIRCLE,

    /** Cuadrado redondeado (Trabajo). */
    SQUARE,

    /** Rombo (Personal). */
    DIAMOND,
    ;

    /** Conversión desde el texto guardado en la base de datos. */
    companion object {
        /** Convierte el texto guardado en la base de datos; si no se reconoce usa [CIRCLE]. */
        fun fromName(name: String): CategoryShape = entries.firstOrNull { it.name == name } ?: CIRCLE
    }
}
