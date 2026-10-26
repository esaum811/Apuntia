package com.apuntia.app.ui.navigation

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

    /** Nombre del dato que lleva el id de la entrada a editar. */
    const val ENTRY_ID_ARG = "entryId"

    /** Patrón de la pantalla del editor; el id es opcional (sin id se crea una nota nueva). */
    const val EDITOR = "editor?$ENTRY_ID_ARG={$ENTRY_ID_ARG}"

    /** Ruta para abrir el editor con una entrada existente, o vacío si [entryId] es nulo. */
    fun editor(entryId: String? = null): String = if (entryId == null) "editor" else "editor?$ENTRY_ID_ARG=$entryId"
}
