package com.apuntia.app.ui.vault

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.security.PinRules
import com.apuntia.app.security.UnlockResult
import com.apuntia.app.security.Vault
import com.apuntia.app.security.VaultState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "Apuntia"

/** Avisos que la pantalla del PIN puede mostrar. */
sealed interface VaultMessage {
    /** El PIN no tiene entre 4 y 8 números. */
    data object InvalidPin : VaultMessage

    /** Los dos PIN escritos al crearlo no coinciden. */
    data object PinsDontMatch : VaultMessage

    /**
     * El PIN era incorrecto; quedan [attemptsLeft] intentos antes de una espera o, si [willWipe],
     * antes de que se borren las notas confidenciales.
     */
    data class WrongPin(val attemptsLeft: Int, val willWipe: Boolean) : VaultMessage

    /** Falló algo inesperado (por ejemplo, el Keystore del teléfono). */
    data object Failed : VaultMessage
}

/**
 * Lo que la pantalla del PIN necesita para dibujarse.
 *
 * @property message aviso a mostrar, o nulo.
 * @property lockedUntil hora (milisegundos) hasta la que hay que esperar por demasiados intentos; 0 si no hay espera.
 * @property finalAttemptsLeft intentos que quedan antes de borrar las notas confidenciales, o nulo si aún no aplica.
 * @property isWorking verdadero mientras se comprueba el PIN; evita enviarlo dos veces.
 */
data class VaultUiState(
    val message: VaultMessage? = null,
    val lockedUntil: Long = 0L,
    val finalAttemptsLeft: Int? = null,
    val isWorking: Boolean = false,
)

/** Atiende crear el PIN y abrir la bóveda. */
class VaultViewModel(
    private val vault: Vault,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val mutableState = MutableStateFlow(VaultUiState(finalAttemptsLeft = vault.finalAttemptsLeft()))

    /** Texto del PIN. */
    val pinState = TextFieldState()

    /** Texto de la repetición del PIN (solo al crearlo). */
    val confirmState = TextFieldState()

    /** Situación de la bóveda: sin PIN, cerrada o abierta. */
    val vaultState: StateFlow<VaultState> = vault.state

    /** Estado actual de la pantalla. */
    val uiState: StateFlow<VaultUiState> = mutableState.asStateFlow()

    /** Crea el PIN o intenta abrir la bóveda, según haga falta. */
    fun submit() {
        if (mutableState.value.isWorking) return
        mutableState.update { it.copy(isWorking = true, message = null) }
        viewModelScope.launch {
            @Suppress("TooGenericExceptionCaught")
            try {
                if (vault.state.value == VaultState.NO_PIN) create() else unlock()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Error con la bóveda", error)
                mutableState.update { it.copy(message = VaultMessage.Failed) }
            } finally {
                pinState.clearText()
                confirmState.clearText()
                mutableState.update { it.copy(isWorking = false, finalAttemptsLeft = vault.finalAttemptsLeft()) }
            }
        }
    }

    private suspend fun create() {
        val pin = pinState.text.toString()
        when {
            !PinRules.isValid(pin) -> mutableState.update { it.copy(message = VaultMessage.InvalidPin) }
            pin != confirmState.text.toString() -> mutableState.update { it.copy(message = VaultMessage.PinsDontMatch) }
            else -> vault.createPin(pin)
        }
    }

    private suspend fun unlock() {
        when (val result = vault.unlock(pinState.text.toString())) {
            UnlockResult.Success -> Unit
            is UnlockResult.WrongPin ->
                mutableState.update {
                    it.copy(message = VaultMessage.WrongPin(result.attemptsLeft, result.willWipe))
                }
            // Al borrarse todo, la bóveda queda sin PIN; la pantalla y el editor reaccionan solos a ese cambio.
            UnlockResult.Wiped -> Unit
            is UnlockResult.LockedOut ->
                mutableState.update { it.copy(lockedUntil = clock() + result.remainingMillis) }
        }
    }
}
