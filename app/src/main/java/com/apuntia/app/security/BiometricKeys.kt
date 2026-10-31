package com.apuntia.app.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "apuntia_vault_biometric_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val AES_KEY_BITS = 256
private const val GCM_TAG_BITS = 128

/**
 * Llave del Android Keystore que solo se puede usar justo después de reconocer la huella.
 * Si el usuario registra otra huella en el teléfono, Android la invalida y hay que volver a activarla.
 */
class BiometricKeys {
    /** Crea una llave nueva y devuelve un cifrador listo para que la huella lo autorice. */
    fun newEnrollCipher(): Cipher {
        delete()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, createKey())
        return cipher
    }

    /**
     * Devuelve un cifrador listo para que la huella lo autorice y descifre [seal].
     * Devuelve nulo si la llave ya no sirve (por ejemplo, se registró otra huella en el teléfono).
     */
    fun unlockCipher(seal: BiometricSeal): Cipher? {
        val key = loadKey() ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        return try {
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, seal.iv))
            cipher
        } catch (_: KeyPermanentlyInvalidatedException) {
            null
        }
    }

    /** Borra la llave de la huella. */
    fun delete() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
    }

    private fun loadKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
    }

    private fun createKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val spec = KeyGenParameterSpec.Builder(KEY_ALIAS, purposes)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_BITS)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
        // Desde Android 11 se indica que la llave pide huella fuerte en cada uso.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            spec.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        }
        generator.init(spec.build())
        return generator.generateKey()
    }
}
