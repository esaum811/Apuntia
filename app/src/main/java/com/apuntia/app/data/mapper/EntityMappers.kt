package com.apuntia.app.data.mapper

import com.apuntia.app.data.local.entity.CategoryEntity
import com.apuntia.app.data.local.entity.EntryEntity
import com.apuntia.app.data.local.entity.ReminderEntity
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.CategoryShape
import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.model.Reminder

/** Convierte una categoría de la base de datos en una del dominio. */
fun CategoryEntity.toDomain() = Category(id, name, CategoryShape.fromName(shape), color, sortOrder, isDefault)

/** Convierte una categoría del dominio en una para la base de datos. */
fun Category.toEntity() = CategoryEntity(id, name, shape.name, color, sortOrder, isDefault)

/** Convierte una entrada de la base de datos en una del dominio. El texto cifrado nunca sale de aquí. */
fun EntryEntity.toDomain() = Entry(
    id = id,
    categoryId = categoryId,
    type = type,
    title = title,
    body = body,
    isDone = isDone,
    dueAt = dueAt,
    isConfidential = isConfidential,
    isPinned = isPinned,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

/** Convierte una entrada del dominio en una para la base de datos, con su contenido cifrado si es confidencial. */
fun Entry.toEntity(bodyEncrypted: ByteArray? = null) = EntryEntity(
    id = id,
    categoryId = categoryId,
    type = type,
    title = title,
    body = body,
    isDone = isDone,
    dueAt = dueAt,
    isConfidential = isConfidential,
    bodyEncrypted = bodyEncrypted,
    isPinned = isPinned,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

/** Convierte una alarma de la base de datos en una del dominio. */
fun ReminderEntity.toDomain() = Reminder(
    id = id,
    entryId = entryId,
    categoryId = categoryId,
    label = label,
    triggerAt = triggerAt,
    repeatRule = repeatRule,
    ringtoneUri = ringtoneUri,
    isEnabled = isEnabled,
    createdAt = createdAt,
)

/** Convierte una alarma del dominio en una para la base de datos. */
fun Reminder.toEntity() = ReminderEntity(
    id = id,
    entryId = entryId,
    categoryId = categoryId,
    label = label,
    triggerAt = triggerAt,
    repeatRule = repeatRule,
    ringtoneUri = ringtoneUri,
    isEnabled = isEnabled,
    createdAt = createdAt,
)
