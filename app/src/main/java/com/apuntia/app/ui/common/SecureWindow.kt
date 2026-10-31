package com.apuntia.app.ui.common

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

// Cuántas pantallas piden proteger la ventana a la vez; la protección se quita cuando ya nadie la pide.
private var secureRequests = 0

/**
 * Mientras esté en pantalla, impide capturas de pantalla y oculta la app en la vista de apps recientes.
 * Úsalo en toda pantalla que muestre contenido confidencial.
 */
@Composable
fun SecureWindow() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.findActivity()?.window
        secureRequests++
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            secureRequests--
            if (secureRequests == 0) window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
