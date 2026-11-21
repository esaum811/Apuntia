package com.apuntia.app.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "apuntia_settings"
private const val KEY_DONE_CLEANUP = "done_cleanup"
private const val KEY_RINGTONE = "default_ringtone"
private const val KEY_THEME = "theme"
private const val KEY_AUTO_LOCK = "auto_lock"
private const val WEEK_DAYS = 7
private const val MONTH_DAYS = 30
private const val THIRTY_SECONDS_MS = 30_000L
private const val ONE_MINUTE_MS = 60_000L
private const val TWO_MINUTES_MS = 120_000L
private const val FIVE_MINUTES_MS = 300_000L

/**
 * Cuánto tiempo se guardan las tareas hechas antes de pasar solas a la papelera.
 *
 * @property days días que deben pasar desde que se marcó como hecha, o nulo si nunca se limpian.
 */
enum class DoneCleanup(val days: Int?) {
    /** Las tareas hechas se quedan hasta que el usuario las vacíe. */
    NEVER(null),

    /** Pasan a la papelera a los 7 días. */
    AFTER_7_DAYS(WEEK_DAYS),

    /** Pasan a la papelera a los 30 días. */
    AFTER_30_DAYS(MONTH_DAYS),
    ;

    internal companion object {
        /** Convierte el nombre guardado en la opción; si no se reconoce, usa [NEVER]. */
        fun fromName(name: String?): DoneCleanup = entries.firstOrNull { it.name == name } ?: NEVER
    }
}

/** Tema de colores de la app. */
enum class ThemeMode {
    /** Sigue el modo claro u oscuro del teléfono. */
    SYSTEM,

    /** Siempre claro. */
    LIGHT,

    /** Siempre oscuro. */
    DARK,
    ;

    internal companion object {
        /** Convierte el nombre guardado en la opción; si no se reconoce, usa [SYSTEM]. */
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/**
 * Tiempo que la app puede estar en segundo plano antes de volver a pedir el PIN de las notas confidenciales.
 *
 * @property millis milisegundos de gracia; cero cierra la bóveda al salir de la app.
 */
enum class AutoLock(val millis: Long) {
    /** Al salir de la app. */
    IMMEDIATE(0L),

    /** A los 30 segundos. */
    SECONDS_30(THIRTY_SECONDS_MS),

    /** Al minuto (valor por defecto). */
    MINUTE_1(ONE_MINUTE_MS),

    /** A los 2 minutos. */
    MINUTES_2(TWO_MINUTES_MS),

    /** A los 5 minutos. */
    MINUTES_5(FIVE_MINUTES_MS),
    ;

    internal companion object {
        /** Convierte el nombre guardado en la opción; si no se reconoce, usa [MINUTE_1]. */
        fun fromName(name: String?): AutoLock = entries.firstOrNull { it.name == name } ?: MINUTE_1
    }
}

/** Preferencias de la app que no son secretas (limpieza de tareas, sonido de alarma, tema y bloqueo automático). */
class AppSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutableCleanup = MutableStateFlow(DoneCleanup.fromName(prefs.getString(KEY_DONE_CLEANUP, null)))
    private val mutableRingtone = MutableStateFlow(prefs.getString(KEY_RINGTONE, null))
    private val mutableTheme = MutableStateFlow(ThemeMode.fromName(prefs.getString(KEY_THEME, null)))
    private val mutableAutoLock = MutableStateFlow(AutoLock.fromName(prefs.getString(KEY_AUTO_LOCK, null)))

    /** Cuándo se limpian las tareas hechas; avisa cuando cambia. */
    val doneCleanup: StateFlow<DoneCleanup> = mutableCleanup.asStateFlow()

    /** Sonido de alarma elegido por defecto, o nulo para usar el del sistema; avisa cuando cambia. */
    val defaultRingtone: StateFlow<String?> = mutableRingtone.asStateFlow()

    /** Tema elegido; avisa cuando cambia. */
    val theme: StateFlow<ThemeMode> = mutableTheme.asStateFlow()

    /** Bloqueo automático de las notas confidenciales; avisa cuando cambia. */
    val autoLock: StateFlow<AutoLock> = mutableAutoLock.asStateFlow()

    /** Cambia cuándo se limpian las tareas hechas. */
    fun setDoneCleanup(option: DoneCleanup) {
        prefs.edit().putString(KEY_DONE_CLEANUP, option.name).apply()
        mutableCleanup.value = option
    }

    /** Cambia el sonido de alarma por defecto; nulo vuelve al del sistema. */
    fun setDefaultRingtone(uri: String?) {
        prefs.edit().putString(KEY_RINGTONE, uri).apply()
        mutableRingtone.value = uri
    }

    /** Cambia el tema de la app. */
    fun setTheme(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        mutableTheme.value = mode
    }

    /** Cambia el tiempo del bloqueo automático. */
    fun setAutoLock(option: AutoLock) {
        prefs.edit().putString(KEY_AUTO_LOCK, option.name).apply()
        mutableAutoLock.value = option
    }
}
