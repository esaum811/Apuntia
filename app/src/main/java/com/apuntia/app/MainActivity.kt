package com.apuntia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.apuntia.app.ui.preview.DesignPreviewScreen
import com.apuntia.app.ui.theme.ApuntiaTheme

/**
 * Actividad principal de Apuntia.
 *
 * Es el único punto de entrada de la interfaz: configura el tema
 * y muestra el contenido en Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApuntiaTheme {
                DesignPreviewScreen()
            }
        }
    }
}

/**
 * Muestra un saludo de prueba.
 *
 * Es contenido de la plantilla inicial y se reemplazará con las pantallas reales.
 *
 * @param name nombre al que se saluda.
 * @param modifier modificador de Compose para ajustar el diseño.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

/**
 * Vista previa de [Greeting] para el editor de Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ApuntiaTheme {
        Greeting("Android")
    }
}
