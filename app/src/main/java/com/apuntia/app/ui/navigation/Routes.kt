package com.apuntia.app.ui.navigation

import com.apuntia.app.domain.model.EntryType

/** Nombres de las pantallas para navegar entre ellas. */
object Routes {
    /** Lista de notas (inicio). */
    const val NOTES = "notes"

    /** Lista de tareas. */
    const val TASKS = "tasks"

    /** Lista de alarmas. */
    const val ALARMS = "alarms"

    /** Ajustes. */
    const val SETTINGS = "settings"

    /** Papelera. */
    const val TRASH = "trash"

    /** Nombre del dato que lleva el id de la alarma a editar. */
    const val ALARM_ID_ARG = "alarmId"

    /** Patrón de la pantalla del editor de alarmas; el id es opcional. */
    const val ALARM_EDITOR = "alarm?$ALARM_ID_ARG={$ALARM_ID_ARG}"

    /** Ruta para abrir el editor de alarmas; [alarmId] nulo crea una alarma nueva. */
    fun alarmEditor(alarmId: String? = null): String = if (alarmId == null) "alarm" else "alarm?$ALARM_ID_ARG=$alarmId"

    /** Nombre del dato que lleva el id de la entrada a editar. */
    const val ENTRY_ID_ARG = "entryId"

    /** Nombre del dato que lleva el tipo (nota o tarea) con el que se abre un editor nuevo. */
    const val TYPE_ARG = "type"

    /** Patrón de la pantalla del editor; el id y el tipo son opcionales. */
    const val EDITOR = "editor?$ENTRY_ID_ARG={$ENTRY_ID_ARG}&$TYPE_ARG={$TYPE_ARG}"

    /**
     * Ruta para abrir el editor.
     *
     * @param entryId entrada a editar, o nulo para crear una nueva.
     * @param type tipo con el que se abre una entrada nueva, o nulo para nota.
     */
    fun editor(entryId: String? = null, type: EntryType? = null): String {
        val params = listOfNotNull(
            entryId?.let { "$ENTRY_ID_ARG=$it" },
            type?.let { "$TYPE_ARG=${it.name}" },
        )
        return if (params.isEmpty()) "editor" else "editor?" + params.joinToString("&")
    }
}
