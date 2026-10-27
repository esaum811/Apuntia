package com.apuntia.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "apuntia_database_key"
private const val PREFS_NAME = "apuntia_secure_prefs"
private const val PREF_PASSPHRASE = "database_passphrase"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val AES_KEY_BITS = 256
private const val GCM_TAG_BITS = 128
private const val PASSPHRASE_BYTES = 32

/**
 * Entrega la contraseña con la que se cifra la base de datos.
 *
 * La contraseña es un valor aleatorio que se crea la primera vez que se abre la app. Se guarda
 * cifrada con una llave del Android Keystore, que nunca sale del teléfono. Así no hay ninguna
 * contraseña escrita en el código ni en texto plano en el almacenamiento.
 *
 * Esto significa que la base de datos solo se puede abrir en este mismo teléfono: por eso la app
 * desactiva el respaldo automático de Android y usa su propio archivo de respaldo cifrado.
 */
class DatabaseKeyProvider(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Devuelve la contraseña de la base de datos; si no existe todavía, la crea y la guarda.
     * Puede lanzar una excepción si el Keystore ya no tiene la llave; quien llama debe manejarlo.
     * Llámala fuera del hilo principal.
     */
    fun getOrCreatePassphrase(): ByteArray {
        val stored = prefs.getString(PREF_PASSPHRASE, null)
        return if (stored != null) decrypt(stored) else createAndStore()
    }

    private fun createAndStore(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        // commit() escribe en el momento: con apply() un cierre inesperado podría perder la contraseña
        // después de haber creado la base de datos, y las notas quedarían ilegibles.
        val saved = prefs.edit().putString(PREF_PASSPHRASE, encrypt(passphrase)).commit()
        check(saved) { "No se pudo guardar la contraseña de la base de datos" }
        return passphrase
    }

    private fun encrypt(plain: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        // El vector de inicialización (iv) se guarda junto al texto cifrado porque se necesita para descifrar.
        val packed = cipher.iv + cipher.doFinal(plain)
        return Base64.encodeToString(packed, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): ByteArray {
        val packed = Base64.decode(encoded, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = packed.copyOfRange(0, GCM_IV_BYTES)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(packed, GCM_IV_BYTES, packed.size - GCM_IV_BYTES)
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

    private companion object {
        /** Tamaño del vector de inicialización que genera Android para AES/GCM. */
        const val GCM_IV_BYTES = 12
    }
}
