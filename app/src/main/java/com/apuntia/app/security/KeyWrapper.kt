package com.apuntia.app.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "apuntia_vault_wrap_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val AES_KEY_BITS = 256
private const val GCM_TAG_BITS = 128
private const val GCM_IV_BYTES = 12

/**
 * Envuelve la llave de la bóveda con una llave que vive en el hardware del teléfono.
 * Así, aunque alguien copie los archivos de la app, no puede probar PINs en otro equipo.
 */
interface KeyWrapper {
    /** Cifra [data] con la llave del teléfono. */
    fun wrap(data: ByteArray): ByteArray

    /** Descifra lo que produjo [wrap]. */
    fun unwrap(data: ByteArray): ByteArray
}

/** Implementación con el Android Keystore. */
class KeystoreKeyWrapper : KeyWrapper {
    override fun wrap(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        // El vector de inicialización (iv) va al principio porque se necesita para descifrar.
        return cipher.iv + cipher.doFinal(data)
    }

    override fun unwrap(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = data.copyOfRange(0, GCM_IV_BYTES)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(data, GCM_IV_BYTES, data.size - GCM_IV_BYTES)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(AES_KEY_BITS)
                .build(),
        )
        return generator.generateKey()
    }
}
