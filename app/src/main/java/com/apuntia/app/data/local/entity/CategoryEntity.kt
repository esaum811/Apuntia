package com.apuntia.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Categoría para ordenar las entradas (Escuela, Trabajo, Personal o una creada por el usuario).
 *
 * @property id identificador único. Las categorías por defecto usan un id fijo para poder
 * insertarlas sin duplicarlas.
 * @property name nombre que se muestra en pantalla.
 * @property shape nombre de la forma elegida (ver `CategoryShape`).
 * @property color color de la categoría en formato ARGB.
 * @property sortOrder posición en la lista; menor número aparece primero.
 * @property isDefault indica si viene incluida con la app y no se puede borrar.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shape: String,
    val color: Long,
    val sortOrder: Int,
    val isDefault: Boolean,
)
