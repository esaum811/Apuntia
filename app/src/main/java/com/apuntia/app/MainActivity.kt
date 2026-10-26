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
