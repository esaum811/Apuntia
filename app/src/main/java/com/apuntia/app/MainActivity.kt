package com.apuntia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.apuntia.app.ui.navigation.ApuntiaApp
import com.apuntia.app.ui.theme.ApuntiaTheme

/**
 * Actividad principal de Apuntia.
 *
 * Es el único punto de entrada de la interfaz: configura el tema y muestra la app en Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
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
            ApuntiaTheme {
                ApuntiaApp()
            }
        }
    }
}
