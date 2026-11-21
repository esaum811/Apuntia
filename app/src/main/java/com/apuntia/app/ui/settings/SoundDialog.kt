package com.apuntia.app.ui.settings

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apuntia.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "Apuntia"
private val ListMaxHeight = 320.dp

/** Un sonido de alarma que ofrece el teléfono. */
internal data class SoundOption(val uri: String, val title: String)

/**
 * Lista de sonidos de alarma dentro de la app, con vista previa al tocar cada uno.
 *
 * No usa el selector del sistema: en algunos teléfonos ese selector cerraba la app.
 *
 * @param current sonido guardado, o nulo si se usa el del sistema.
 * @param onSave se llama con el sonido elegido (nulo para el del sistema).
 */
@Composable
internal fun SoundDialog(current: String?, onSave: (String?) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sounds by produceState(initialValue = emptyList<SoundOption>(), context) {
        value = withContext(Dispatchers.IO) { loadAlarmSounds(context) }
    }
    var selected by rememberSaveable { mutableStateOf(current) }
    val preview = remember { SoundPreview(context) }
    DisposableEffect(Unit) { onDispose { preview.stop() } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_sound_title)) },
        text = {
            LazyColumn(Modifier.heightIn(max = ListMaxHeight).selectableGroup()) {
                item(key = "system") {
                    OptionRow(stringResource(R.string.settings_sound_system), selected == null) {
                        selected = null
                        preview.play(null)
                    }
                }
                items(sounds, key = { it.uri }) { sound ->
                    OptionRow(sound.title, selected == sound.uri) {
                        selected = sound.uri
                        preview.play(sound.uri)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(selected) }) { Text(stringResource(R.string.prefs_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}

/** Nombre legible del sonido guardado, o nulo si no hay uno elegido o ya no existe. */
@Suppress("TooGenericExceptionCaught")
internal fun ringtoneTitle(context: Context, uri: String?): String? = uri?.let {
    try {
        RingtoneManager.getRingtone(context, Uri.parse(it))?.getTitle(context)
    } catch (error: RuntimeException) {
        Log.w(TAG, "No se pudo leer el nombre del sonido", error)
        null
    }
}

// Sonidos de alarma del teléfono; si el sistema falla, devuelve una lista vacía y queda solo el predeterminado.
@Suppress("TooGenericExceptionCaught")
private fun loadAlarmSounds(context: Context): List<SoundOption> = try {
    val manager = RingtoneManager(context).apply { setType(RingtoneManager.TYPE_ALARM) }
    manager.cursor.use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX).orEmpty()
                add(SoundOption(manager.getRingtoneUri(cursor.position).toString(), title))
            }
        }
    }
} catch (error: RuntimeException) {
    Log.e(TAG, "No se pudieron leer los sonidos de alarma", error)
    emptyList()
}

// Reproduce una muestra del sonido tocado; solo suena uno a la vez.
private class SoundPreview(private val context: Context) {
    private var ringtone: Ringtone? = null

    @Suppress("TooGenericExceptionCaught")
    fun play(uri: String?) {
        stop()
        ringtone = try {
            val target = uri?.let(Uri::parse) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            RingtoneManager.getRingtone(context, target)?.also {
                it.audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                it.play()
            }
        } catch (error: RuntimeException) {
            Log.w(TAG, "No se pudo reproducir la muestra", error)
            null
        }
    }

    fun stop() {
        ringtone?.stop()
        ringtone = null
    }
}
