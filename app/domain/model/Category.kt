package com.apuntia.app.domain.model

/**
 * Categoría para ordenar las entradas.
 *
 * @property id identificador único.
 * @property name nombre que se muestra en pantalla.
 * @property shape forma que acompaña al color.
 * @property color color en formato ARGB (por ejemplo `0xFFFFD84D`).
 * @property sortOrder posición en la lista; menor número aparece primero.
 * @property isDefault indica si viene incluida con la app y no se puede borrar.
 */
data class Category(
    val id: String,
    val name: String,
    val shape: CategoryShape,
    val color: Long,
    val sortOrder: Int,
    val isDefault: Boolean,
)
