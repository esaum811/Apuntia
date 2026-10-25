package com.apuntia.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.apuntia.app.domain.model.RepeatRule

/**
 * Una alarma. Puede estar ligada a una entrada o ser independiente.
 *
 * Al reiniciar el teléfono se leen las alarmas activas de esta tabla para volver a programarlas.
 *
 * @property id identificador único (UUID).
 * @property entryId entrada relacionada, o nulo si es una alarma suelta.
 * @property label texto que se muestra al sonar la alarma.
 * @property triggerAt próxima fecha y hora en que debe sonar (milisegundos UTC).
 * @property repeatRule cada cuánto se repite.
 * @property ringtoneUri sonido elegido, o nulo para usar el predeterminado.
 * @property isEnabled indica si la alarma está activa.
 * @property createdAt momento de creación.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("entryId"), Index("isEnabled")],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val entryId: String?,
    val label: String,
    val triggerAt: Long,
    val repeatRule: RepeatRule,
    val ringtoneUri: String?,
    val isEnabled: Boolean,
    val createdAt: Long,
)
