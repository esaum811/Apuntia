package com.apuntia.app.alarm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Dice a la pantalla de alarma si hay una alarma sonando, para que se cierre sola cuando termine. */
object AlarmState {
    private val mutableRinging = MutableStateFlow(false)

    /** Verdadero mientras suena una alarma. */
    val isRinging: StateFlow<Boolean> = mutableRinging.asStateFlow()

    /** Lo usa el servicio de alarma al empezar y al terminar de sonar. */
    internal fun setRinging(value: Boolean) {
        mutableRinging.value = value
    }
}
