package com.apuntia.app.domain.model

/** Grupos en los que se agrupan las tareas pendientes según su fecha límite, en el orden en que se muestran. */
enum class TaskSectionType {
    /** Fecha límite en un día anterior a hoy. */
    OVERDUE,

    /** Fecha límite hoy, aunque la hora ya haya pasado. */
    TODAY,

    /** Fecha límite de mañana a los próximos 6 días. */
    THIS_WEEK,

    /** Fecha límite más adelante. */
    LATER,

    /** Sin fecha límite. */
    NO_DATE,
}

/**
 * Un grupo de tareas con la misma [type] de fecha límite.
 *
 * @property type a qué grupo pertenecen.
 * @property tasks tareas del grupo, de la que vence primero a la última.
 */
data class TaskSection(val type: TaskSectionType, val tasks: List<Entry>)
