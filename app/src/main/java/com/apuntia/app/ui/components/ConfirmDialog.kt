package com.apuntia.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.apuntia.app.R

/**
 * Pregunta de confirmación antes de una acción que no se puede deshacer fácilmente.
 *
 * @param confirmLabel texto del botón de confirmar; empieza con un verbo ("Vaciar", "Eliminar").
 * @param onConfirm se llama si el usuario confirma.
 * @param onDismiss se llama si cancela o toca fuera del cuadro.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}
