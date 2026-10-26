package com.apuntia.app.domain.repository

import com.apuntia.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

/** Acceso a las categorías sin depender de cómo se guardan. */
interface CategoryRepository {
    /** Todas las categorías en orden; avisa cuando cambian. */
    fun observeAll(): Flow<List<Category>>

    /** Crea las categorías por defecto (Escuela, Trabajo, Personal) si todavía no existen. */
    suspend fun ensureDefaults()

    /** Crea o actualiza una [category]. */
    suspend fun save(category: Category)

    /** Borra una categoría creada por el usuario. Las categorías por defecto no se borran. */
    suspend fun deleteCustom(id: String)
}
