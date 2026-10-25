package com.apuntia.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.apuntia.app.domain.model.EntryType

/**
 * Una nota o una tarea. Comparten tabla para simplificar la interfaz y la búsqueda.
 *
 * Las fechas son milisegundos UTC. Una entrada con [deletedAt] distinto de nulo está en la papelera.
 *
 * @property id identificador único (UUID).
 * @property categoryId categoría a la que pertenece.
 * @property type nota o tarea.
 * @property title título de la entrada.
 * @property body contenido en texto. Queda vacío si la entrada es confidencial.
 * @property isDone indica si la tarea está terminada (solo aplica a tareas).
 * @property dueAt fecha límite (solo aplica a tareas).
 * @property isConfidential indica si el contenido se guarda cifrado y requiere PIN o huella.
 * @property bodyEncrypted contenido cifrado cuando [isConfidential] es verdadero.
 * @property isPinned indica si aparece fija al inicio de la lista.
 * @property createdAt momento de creación.
 * @property updatedAt momento de la última modificación.
 * @property deletedAt momento en que se envió a la papelera, o nulo si está activa.
 */
@Suppress("ArrayInDataClass") // Room solo guarda el arreglo; nunca se compara una entrada por igualdad de bytes.
@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("deletedAt"), Index("updatedAt")],
)
data class EntryEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val type: EntryType,
    val title: String,
    val body: String,
    val isDone: Boolean,
    val dueAt: Long?,
    val isConfidential: Boolean,
    val bodyEncrypted: ByteArray?,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)
