package com.apuntia.app.data.local.database

import android.content.Context
import androidx.room3.Room
import kotlinx.coroutines.Dispatchers
import net.zetetic.database.sqlcipher.driver.SQLCipherDriver

private const val DATABASE_NAME = "apuntia.db"
private const val SQLCIPHER_LIBRARY = "sqlcipher"

/** Crea la base de datos de Apuntia cifrada con SQLCipher. */
object ApuntiaDatabaseFactory {

    /**
     * Abre (o crea) la base de datos cifrada.
     *
     * @param context contexto de la app.
     * @param passphrase contraseña de cifrado, que entrega [com.apuntia.app.security.DatabaseKeyProvider].
     */
    fun create(context: Context, passphrase: ByteArray): ApuntiaDatabase {
        // SQLCipher trae su propia librería nativa y hay que cargarla antes de abrir la base.
        System.loadLibrary(SQLCIPHER_LIBRARY)
        val databasePath = context.applicationContext.getDatabasePath(DATABASE_NAME).absolutePath
        return Room.databaseBuilder<ApuntiaDatabase>(context.applicationContext, databasePath)
            .setQueryCoroutineContext(Dispatchers.IO)
            // El driver va al final: después de fijarlo, Room deja de usar las APIs antiguas de SQLite.
            .setDriver(SQLCipherDriver(passphrase, null, null))
            .build()
    }
}
