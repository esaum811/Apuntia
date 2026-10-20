package com.apuntia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val lightScheme = lightColorScheme(
    primary = lightBlue,
    onPrimary = lightOnBlue,
    primaryContainer = lightBlueSoft,
    onPrimaryContainer = lightOnBlueSoft,
    secondary = lightOrange,
    onSecondary = lightOnOrange,
    secondaryContainer = lightOrangeSoft,
    onSecondaryContainer = lightOnOrangeSoft,
    background = lightBackground,
    onBackground = lightOnSurface,
    surface = lightSurface,
    onSurface = lightOnSurface,
    surfaceVariant = lightSurfaceVariant,
    onSurfaceVariant = lightOnSurfaceVariant,
    outline = lightOutline,
    outlineVariant = lightOutlineVariant,
    error = lightError,
    onError = lightOnError,
)

private val darkScheme = darkColorScheme(
    primary = darkBlue,
    onPrimary = darkOnBlue,
    primaryContainer = darkBlueSoft,
    onPrimaryContainer = darkOnBlueSoft,
    secondary = darkOrange,
    onSecondary = darkOnOrange,
    secondaryContainer = darkOrangeSoft,
    onSecondaryContainer = darkOnOrangeSoft,
    background = darkBackground,
    onBackground = darkOnSurface,
    surface = darkSurface,
    onSurface = darkOnSurface,
    surfaceVariant = darkSurfaceVariant,
    onSurfaceVariant = darkOnSurfaceVariant,
    outline = darkOutline,
    outlineVariant = darkOutlineVariant,
    error = darkError,
    onError = darkOnError,
)

/**
 * Tema visual de Apuntia (colores y tipografía de Material 3).
 *
 * Sigue el modo claro u oscuro del teléfono. No usa los colores dinámicos de Android 12
 * a propósito: así la app conserva siempre su identidad azul y naranja.
 *
 * @param darkTheme indica si se usa el modo oscuro; por defecto, el del sistema.
 * @param content contenido de la interfaz al que se aplica el tema.
 */
@Composable
fun ApuntiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
        typography = apuntiaTypography,
        content = content,
    )
}
