package com.apuntia.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apuntia.app.settings.ThemeMode
import com.apuntia.app.ui.navigation.ApuntiaApp
import com.apuntia.app.ui.theme.ApuntiaTheme

/**
 * Actividad principal de Apuntia.
 *
 * Extiende `FragmentActivity` porque el diálogo de la huella lo necesita.
 * Es el único punto de entrada de la interfaz: configura el tema y muestra la app en Jetpack Compose.
 */
class MainActivity : FragmentActivity() {
    private val autoLock get() = (application as ApuntiaApplication).container.vaultAutoLock

    override fun onStart() {
        super.onStart()
        autoLock.onForegrounded()
    }

    override fun onStop() {
        super.onStop()
        // Girar la pantalla también detiene la actividad; eso no cuenta como salir de la app.
        if (!isChangingConfigurations) autoLock.onBackgrounded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mode by (application as ApuntiaApplication).container.settings.theme.collectAsStateWithLifecycle()
            val dark = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Los íconos de las barras del sistema deben seguir el tema elegido, no el del teléfono.
            DisposableEffect(dark) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            ApuntiaTheme(darkTheme = dark) {
                ApuntiaApp()
            }
        }
    }
}
