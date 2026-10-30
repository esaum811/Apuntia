package com.apuntia.app.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_BITS = 128
private const val IV_BYTES = 12

/** Cifrado AES-GCM: además de ocultar el texto, detecta si alguien lo modificó o si la llave es incorrecta. */
internal object AesGcm {
    private val random = SecureRandom()

    /** Cifra [plain] con [key]. El resultado lleva el vector de inicialización al principio. */
    fun encrypt(key: ByteArray, plain: ByteArray): ByteArray {
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        return iv + cipher.doFinal(plain)
    }

    /**
     * Descifra lo que produjo [encrypt].
     * Lanza `GeneralSecurityException` si la llave no corresponde o el contenido fue alterado.
     */
    fun decrypt(key: ByteArray, blob: ByteArray): ByteArray {
        require(blob.size > IV_BYTES) { "El contenido cifrado está incompleto" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = blob.copyOfRange(0, IV_BYTES)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(blob, IV_BYTES, blob.size - IV_BYTES)
    }
}
