package com.apuntia.app.data.repository

import com.apuntia.app.data.local.dao.CategoryDao
import com.apuntia.app.data.local.database.DefaultCategories
import com.apuntia.app.data.mapper.toDomain
import com.apuntia.app.data.mapper.toEntity
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Guarda las categorías en la base de datos local. */
class RoomCategoryRepository(private val dao: CategoryDao) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun ensureDefaults() = dao.insertAllIfAbsent(DefaultCategories.all)

    override suspend fun save(category: Category) = dao.upsert(category.toEntity())

    override suspend fun deleteCustom(id: String) = dao.deleteCustom(id)
}
