package com.apuntia.app.ui.vault

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

/** Indica si el teléfono tiene huella (o rostro seguro) registrada y lista para usar. */
fun isBiometricAvailable(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Muestra el diálogo del sistema para reconocer la huella y, si sale bien, entrega el [cipher] ya autorizado.
 *
 * @param negativeText texto del botón para cancelar, por ejemplo "Usar PIN".
 * @param onSuccess se llama con el cifrador autorizado.
 * @param onError se llama si el usuario cancela o el diálogo se cierra por un error.
 */
fun authenticateWithBiometric(
    activity: FragmentActivity,
    cipher: Cipher,
    title: String,
    negativeText: String,
    onError: () -> Unit = {},
    onSuccess: (Cipher) -> Unit,
) {
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            result.cryptoObject?.cipher?.let(onSuccess)
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onError()
        }
    }
    val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setNegativeButtonText(negativeText)
        .setAllowedAuthenticators(BIOMETRIC_STRONG)
        .setConfirmationRequired(false)
        .build()
    prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
}
