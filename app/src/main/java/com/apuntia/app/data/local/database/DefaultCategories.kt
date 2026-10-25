package com.apuntia.app.data.local.database

import com.apuntia.app.data.local.entity.CategoryEntity
import com.apuntia.app.domain.model.CategoryShape

private const val YELLOW = 0xFFFFD84DL
private const val LAVENDER = 0xFF8FA8FFL
private const val PINK = 0xFFFF9DBBL

/** Categorías que trae la app desde el primer arranque, con ids fijos para no duplicarlas. */
object DefaultCategories {
    /** Lista en el orden en que se muestran. */
    val all: List<CategoryEntity> = listOf(
        CategoryEntity("school", "Escuela", CategoryShape.CIRCLE.name, YELLOW, 0, true),
        CategoryEntity("work", "Trabajo", CategoryShape.SQUARE.name, LAVENDER, 1, true),
        CategoryEntity("personal", "Personal", CategoryShape.DIAMOND.name, PINK, 2, true),
    )
}
