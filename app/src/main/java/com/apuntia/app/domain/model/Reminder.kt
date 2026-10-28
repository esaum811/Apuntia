package com.apuntia.app.domain.model

/**
 * Una alarma. Puede estar ligada a una entrada o ser independiente.
 *
 * @property id identificador único.
 * @property entryId entrada relacionada, o nulo si es una alarma suelta.
 * @property categoryId categoría de la alarma.
 * @property label texto que se muestra al sonar.
 * @property triggerAt próxima fecha y hora en que debe sonar (milisegundos UTC).
 * @property repeatRule cada cuánto se repite.
 * @property ringtoneUri sonido elegido, o nulo para usar el predeterminado.
 * @property isEnabled indica si la alarma está activa.
 * @property createdAt momento de creación.
 */
data class Reminder(
    val id: String,
    val entryId: String?,
    val categoryId: String,
    val label: String,
    val triggerAt: Long,
    val repeatRule: RepeatRule,
    val ringtoneUri: String?,
    val isEnabled: Boolean,
    val createdAt: Long,
)
