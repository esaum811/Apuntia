package com.apuntia.app.domain.model

/**
 * Una nota o una tarea.
 *
 * Las fechas son milisegundos UTC. Una entrada con [deletedAt] distinto de nulo está en la papelera.
 *
 * @property id identificador único.
 * @property categoryId categoría a la que pertenece.
 * @property type nota o tarea.
 * @property title título; se muestra siempre, incluso si la entrada es confidencial.
 * @property body contenido en texto. En entradas confidenciales no se llena desde la lista.
 * @property isDone indica si la tarea está terminada.
 * @property dueAt fecha límite de una tarea, o nulo.
 * @property isConfidential indica si el contenido requiere PIN o huella.
 * @property isPinned indica si aparece fija al inicio.
 * @property createdAt momento de creación.
 * @property updatedAt momento de la última modificación.
 * @property deletedAt momento en que pasó a la papelera, o nulo si está activa.
 */
data class Entry(
    val id: String,
    val categoryId: String,
    val type: EntryType,
    val title: String,
    val body: String,
    val isDone: Boolean,
    val dueAt: Long?,
    val isConfidential: Boolean,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)
