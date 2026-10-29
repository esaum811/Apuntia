package com.apuntia.app.security

import android.content.Context

private const val PREFS_NAME = "apuntia_vault"

/** Lugar donde la bóveda guarda sus datos (ya protegidos); se cambia por uno falso en las pruebas. */
interface VaultStorage {
    /** Lee el texto guardado con [key], o nulo si no existe. */
    fun read(key: String): String?

    /** Guarda varios valores a la vez. Devuelve falso si no se pudieron guardar. */
    fun write(values: Map<String, String>): Boolean
}

/** Guarda los datos de la bóveda en un archivo privado de la app. */
class SharedPrefsVaultStorage(context: Context) : VaultStorage {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(key: String): String? = prefs.getString(key, null)

    // commit() escribe en el momento: un cierre inesperado no debe dejar el PIN a medias.
    override fun write(values: Map<String, String>): Boolean {
        val editor = prefs.edit()
        values.forEach { (key, value) -> editor.putString(key, value) }
        return editor.commit()
    }
}
