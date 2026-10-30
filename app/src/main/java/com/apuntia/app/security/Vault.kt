package com.apuntia.app.security

import kotlinx.coroutines.flow.StateFlow

/** Situación de la bóveda de las notas confidenciales. */
enum class VaultState {
    /** Todavía no se creó un PIN. */
    NO_PIN,

    /** Hay un PIN y la bóveda está cerrada: no se puede leer ni guardar contenido confidencial. */
    LOCKED,

    /** La bóveda está abierta. */
    UNLOCKED,
}

/** Resultado de intentar abrir la bóveda con un PIN. */
sealed interface UnlockResult {
    /** El PIN era correcto y la bóveda quedó abierta. */
    data object Success : UnlockResult

    /**
     * El PIN era incorrecto.
     *
     * @property attemptsLeft intentos que quedan antes del siguiente paso.
     * @property willWipe verdadero si ese siguiente paso es borrar las notas confidenciales para siempre.
     */
    data class WrongPin(val attemptsLeft: Int, val willWipe: Boolean) : UnlockResult

    /**
     * Hubo demasiados intentos fallidos y hay que esperar.
     *
     * @property remainingMillis tiempo que falta para poder intentar de nuevo.
     */
    data class LockedOut(val remainingMillis: Long) : UnlockResult

    /** Se agotaron todos los intentos: las notas confidenciales se borraron y se quitó el PIN. */
    data object Wiped : UnlockResult
}

/**
 * Guarda y protege el contenido de las notas confidenciales.
 *
 * El contenido se cifra con una llave que solo existe abierta mientras la bóveda está desbloqueada.
 * No hay forma de recuperar el PIN: si se olvida, el contenido cifrado no se puede leer.
 */
interface Vault {
    /** Situación actual; avisa cuando cambia. */
    val state: StateFlow<VaultState>

    /** Crea el PIN y deja la bóveda abierta. Solo se puede usar cuando [state] es [VaultState.NO_PIN]. */
    suspend fun createPin(pin: String)

    /** Intenta abrir la bóveda con [pin]. */
    suspend fun unlock(pin: String): UnlockResult

    /**
     * Intentos que quedan antes de que se borren las notas confidenciales, o nulo mientras
     * todavía no se llega a esa etapa.
     */
    fun finalAttemptsLeft(): Int?

    /** Cierra la bóveda; hay que volver a poner el PIN para leer contenido confidencial. */
    fun lock()

    /** Cifra [plain]. La bóveda debe estar abierta. */
    fun encrypt(plain: String): ByteArray

    /** Descifra [blob], producido por [encrypt]. La bóveda debe estar abierta. */
    fun decrypt(blob: ByteArray): String
}

/** Reglas que debe cumplir un PIN. */
object PinRules {
    /** Cantidad mínima de números. */
    const val MIN_LENGTH = 4

    /** Cantidad máxima de números. */
    const val MAX_LENGTH = 8

    /** Indica si [pin] son solo números (del 0 al 9) y tiene entre [MIN_LENGTH] y [MAX_LENGTH]. */
    fun isValid(pin: String): Boolean = pin.length in MIN_LENGTH..MAX_LENGTH && pin.all { it in '0'..'9' }
}
