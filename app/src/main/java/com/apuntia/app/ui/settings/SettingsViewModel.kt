package com.apuntia.app.ui.settings

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apuntia.app.security.BiometricKeys
import com.apuntia.app.security.BiometricSeal
import com.apuntia.app.security.PinRules
import com.apuntia.app.security.UnlockResult
import com.apuntia.app.security.Vault
import com.apuntia.app.security.VaultState
import com.apuntia.app.ui.vault.VaultMessage
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "Apuntia"

/**
 * Diálogo para cambiar el PIN.
 *
 * @property message aviso a mostrar, o nulo.
 * @property isWorking verdadero mientras se cambia el PIN.
 */
data class ChangePinState(val message: VaultMessage? = null, val isWorking: Boolean = false)

/**
 * Lo que la pantalla de Ajustes necesita para dibujarse.
 *
 * @property hasPin verdadero si ya se creó un PIN.
 * @property biometricEnabled verdadero si la huella está activada para abrir la bóveda.
 * @property changePin estado del diálogo de cambio de PIN, o nulo si está cerrado.
 * @property wipedEverything verdadero si, al cambiar el PIN, se agotaron los intentos y se borraron las notas.
 */
data class SettingsUiState(
    val hasPin: Boolean = false,
    val biometricEnabled: Boolean = false,
    val changePin: ChangePinState? = null,
    val wipedEverything: Boolean = false,
)

/** Atiende las opciones de seguridad de Ajustes: huella y cambio de PIN. */
class SettingsViewModel(
    private val vault: Vault,
    private val biometricKeys: BiometricKeys,
) : ViewModel() {
    private val mutableState = MutableStateFlow(currentState())

    /** PIN actual que se escribe en el diálogo de cambio de PIN. */
    val currentPinState = TextFieldState()

    /** PIN nuevo que se escribe en el diálogo de cambio de PIN. */
    val newPinState = TextFieldState()

    /** Repetición del PIN nuevo. */
    val confirmPinState = TextFieldState()

    /** Situación de la bóveda. */
    val vaultState: StateFlow<VaultState> = vault.state

    /** Estado actual de la pantalla. */
    val uiState: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    /** Vuelve a leer el estado de la bóveda; se llama al volver a la pantalla. */
    fun refresh() = mutableState.update {
        it.copy(hasPin = vault.state.value != VaultState.NO_PIN, biometricEnabled = vault.biometricSeal() != null)
    }

    private fun currentState() = SettingsUiState(
        hasPin = vault.state.value != VaultState.NO_PIN,
        biometricEnabled = vault.biometricSeal() != null,
    )

    /** Crea la llave de la huella y devuelve el cifrador que el diálogo de huella debe autorizar. */
    fun prepareEnrollCipher(): Cipher? = try {
        biometricKeys.newEnrollCipher()
    } catch (error: GeneralSecurityException) {
        Log.e(TAG, "No se pudo preparar la huella", error)
        null
    }

    /** Guarda la llave de la bóveda protegida por la huella, usando el [cipher] ya autorizado, y cierra la bóveda. */
    fun completeEnableBiometric(cipher: Cipher) {
        try {
            vault.enableBiometric { raw -> BiometricSeal(cipher.iv, cipher.doFinal(raw)) }
            mutableState.update { it.copy(biometricEnabled = true) }
        } catch (error: GeneralSecurityException) {
            Log.e(TAG, "No se pudo activar la huella", error)
            biometricKeys.delete()
        } finally {
            // La bóveda se abrió solo para activar la huella.
            vault.lock()
        }
    }

    /** Cierra la bóveda, por ejemplo si el usuario canceló la activación de la huella. */
    fun lockVault() = vault.lock()

    /** Desactiva la huella; desde entonces solo se abre con el PIN. */
    fun disableBiometric() {
        vault.disableBiometric()
        biometricKeys.delete()
        mutableState.update { it.copy(biometricEnabled = false) }
    }

    /** Abre el diálogo para cambiar el PIN. */
    fun openChangePin() = mutableState.update { it.copy(changePin = ChangePinState()) }

    /** Cierra el diálogo de cambio de PIN y borra lo escrito. */
    fun closeChangePin() {
        clearFields()
        mutableState.update { it.copy(changePin = null) }
    }

    /** Cambia el PIN con lo escrito en el diálogo. */
    fun changePin() {
        val current = currentPinState.text.toString()
        val new = newPinState.text.toString()
        val invalid = when {
            !PinRules.isValid(new) -> VaultMessage.InvalidPin
            new != confirmPinState.text.toString() -> VaultMessage.PinsDontMatch
            else -> null
        }
        if (invalid != null) {
            setMessage(invalid)
        } else if (mutableState.value.changePin?.isWorking == false) {
            runChange(current, new)
        }
    }

    private fun runChange(current: String, new: String) {
        mutableState.update { it.copy(changePin = ChangePinState(isWorking = true)) }
        viewModelScope.launch {
            @Suppress("TooGenericExceptionCaught")
            try {
                handle(vault.changePin(current, new))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Error al cambiar el PIN", error)
                setMessage(VaultMessage.Failed)
            } finally {
                clearFields()
            }
        }
    }

    private fun handle(result: UnlockResult) {
        when (result) {
            UnlockResult.Success -> mutableState.update { it.copy(changePin = null) }
            is UnlockResult.WrongPin -> setMessage(VaultMessage.WrongPin(result.attemptsLeft, result.willWipe))
            is UnlockResult.LockedOut -> setMessage(VaultMessage.LockedOut(result.remainingMillis))
            is UnlockResult.Wiped -> {
                biometricKeys.delete()
                mutableState.update {
                    it.copy(changePin = null, hasPin = false, biometricEnabled = false, wipedEverything = true)
                }
            }
        }
    }

    private fun setMessage(message: VaultMessage) =
        mutableState.update { it.copy(changePin = ChangePinState(message = message)) }

    private fun clearFields() {
        currentPinState.clearText()
        newPinState.clearText()
        confirmPinState.clearText()
    }
}
