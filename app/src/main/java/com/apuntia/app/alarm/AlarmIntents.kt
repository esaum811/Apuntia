package com.apuntia.app.alarm

/** Nombres de las acciones y datos que se intercambian entre las piezas de las alarmas. */
object AlarmIntents {
    /** Acción: una alarma debe sonar ahora. */
    const val ACTION_FIRE = "com.apuntia.app.alarm.FIRE"

    /** Acción: el usuario terminó la alarma que suena. */
    const val ACTION_DISMISS = "com.apuntia.app.alarm.DISMISS"

    /** Acción: el usuario pospuso la alarma que suena. */
    const val ACTION_SNOOZE = "com.apuntia.app.alarm.SNOOZE"

    /** Dato: id de la alarma. */
    const val EXTRA_REMINDER_ID = "reminder_id"

    /** Dato: verdadero si la alarma suena porque se pospuso (no cambia su programación normal). */
    const val EXTRA_IS_SNOOZE = "is_snooze"
}
