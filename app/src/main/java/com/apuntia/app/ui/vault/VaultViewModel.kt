package com.apuntia.app.ui.vault

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.security.BiometricKeys
import com.apuntia.app.security.PinRules
import com.apuntia.app.security.UnlockResult
import com.apuntia.app.security.Vault
import com.apuntia.app.security.VaultState
import java.security.GeneralSecurityException
import javax.crypto.Cipher
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

    /** Hay que esperar [remainingMillis] por demasiados intentos fallidos (lo usa el cambio de PIN). */
    data class LockedOut(val remainingMillis: Long) : VaultMessage

    /** Falló algo inesperado (por ejemplo, el Keystore del teléfono). */
    data object Failed : VaultMessage
}

/**
 * Lo que la pantalla del PIN necesita para dibujarse.
 *
 * @property message aviso a mostrar, o nulo.
 * @property lockedUntil hora (milisegundos) hasta la que hay que esperar por demasiados intentos; 0 si no hay espera.
 * @property finalAttemptsLeft intentos que quedan antes de borrar las notas confidenciales, o nulo si aún no aplica.
 * @property hasBiometric verdadero si la huella está activada para abrir la bóveda.
 * @property wipedEverything nulo si no se borró nada; verdadero si se borraron todas las notas confidenciales;
 * falso si solo se borró la nota que se intentaba abrir.
 * @property isWorking verdadero mientras se comprueba el PIN; evita enviarlo dos veces.
 */
data class VaultUiState(
    val message: VaultMessage? = null,
    val lockedUntil: Long = 0L,
    val finalAttemptsLeft: Int? = null,
    val hasBiometric: Boolean = false,
    val wipedEverything: Boolean? = null,
    val isWorking: Boolean = false,
)

/** Atiende crear el PIN y abrir la bóveda. */
class VaultViewModel(
    private val vault: Vault,
    private val biometricKeys: BiometricKeys,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        VaultUiState(finalAttemptsLeft = vault.finalAttemptsLeft(), hasBiometric = vault.biometricSeal() != null),
    )

    /** Texto del PIN. */
    val pinState = TextFieldState()

    /** Texto de la repetición del PIN (solo al crearlo). */
    val confirmState = TextFieldState()

    /** Situación de la bóveda: sin PIN, cerrada o abierta. */
    val vaultState: StateFlow<VaultState> = vault.state

    /** Estado actual de la pantalla. */
    val uiState: StateFlow<VaultUiState> = mutableState.asStateFlow()

    /**
     * Crea el PIN o intenta abrir la bóveda, según haga falta.
     *
     * @param targetId nota que se quiere abrir; es la que se borra si se agotan los intentos.
     */
    fun submit(targetId: String?) {
        if (mutableState.value.isWorking) return
        mutableState.update { it.copy(isWorking = true, message = null) }
        viewModelScope.launch {
            @Suppress("TooGenericExceptionCaught")
            try {
                if (vault.state.value == VaultState.NO_PIN) create() else unlock(targetId)
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

    /**
     * Prepara el cifrador que la huella debe autorizar para abrir la bóveda.
     * Devuelve nulo si la huella no está activada o ya no sirve (se registró otra huella en el teléfono);
     * en ese caso se desactiva y queda solo el PIN.
     */
    fun prepareBiometricCipher(): Cipher? {
        val seal = vault.biometricSeal() ?: return null
        val cipher = biometricKeys.unlockCipher(seal)
        if (cipher == null) {
            vault.disableBiometric()
            mutableState.update { it.copy(hasBiometric = false) }
        }
        return cipher
    }

    /** Abre la bóveda con el [cipher] que la huella ya autorizó. */
    fun unlockWithBiometric(cipher: Cipher) {
        try {
            vault.unlockWithBiometric { seal -> cipher.doFinal(seal.blob) }
            mutableState.update { it.copy(finalAttemptsLeft = null) }
        } catch (error: GeneralSecurityException) {
            Log.e(TAG, "No se pudo abrir con la huella", error)
            mutableState.update { it.copy(message = VaultMessage.Failed) }
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

    private suspend fun unlock(targetId: String?) {
        when (val result = vault.unlock(pinState.text.toString(), targetId)) {
            UnlockResult.Success -> Unit
            is UnlockResult.WrongPin ->
                mutableState.update {
                    it.copy(message = VaultMessage.WrongPin(result.attemptsLeft, result.willWipe))
                }
            is UnlockResult.Wiped -> mutableState.update { it.copy(wipedEverything = result.everything) }
            is UnlockResult.LockedOut ->
                mutableState.update { it.copy(lockedUntil = clock() + result.remainingMillis) }
        }
    }
}
