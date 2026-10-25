package com.apuntia.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import com.apuntia.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a la tabla de categorías. */
@Dao
interface CategoryDao {

    /** Devuelve todas las categorías ordenadas por posición y avisa cuando cambian. */
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    /** Busca una categoría por su [id], o devuelve nulo si no existe. */
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    /** Cuenta cuántas categorías existen; sirve para saber si ya se crearon las predeterminadas. */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    /** Inserta las [categories] que no existan; si el id ya está, no cambia nada. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfAbsent(categories: List<CategoryEntity>)

    /** Crea o actualiza una [category]. */
    @Upsert
    suspend fun upsert(category: CategoryEntity)

    /** Borra la categoría con el [id] indicado. Falla si todavía tiene entradas. */
    @Query("DELETE FROM categories WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustom(id: String)
}
