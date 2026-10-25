package com.apuntia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colores propios de Apuntia que Material 3 no tiene.
 *
 * @property dock fondo de la barra inferior flotante.
 * @property dockSelected fondo del destino seleccionado en la barra.
 * @property onDockSelected texto e ícono del destino seleccionado.
 * @property dockMuted texto e ícono de los destinos no seleccionados.
 * @property accent color de la acción principal (verde menta).
 * @property onAccent texto sobre [accent].
 */
@Immutable
class ApuntiaColors(
    val dock: Color,
    val dockSelected: Color,
    val onDockSelected: Color,
    val dockMuted: Color,
    val accent: Color,
    val onAccent: Color,
)

private val darkApuntiaColors = ApuntiaColors(
    dock = darkSurface,
    dockSelected = dockSelected,
    onDockSelected = dockOnSelected,
    dockMuted = dockMuted,
    accent = mint,
    onAccent = cardInk,
)

private val lightApuntiaColors = ApuntiaColors(
    dock = darkBackground,
    dockSelected = dockSelected,
    onDockSelected = dockOnSelected,
    dockMuted = dockMuted,
    accent = mint,
    onAccent = cardInk,
)

private val localApuntiaColors = staticCompositionLocalOf { darkApuntiaColors }

/** Acceso a los colores propios: `ApuntiaTheme.colors.accent`. */
object ApuntiaTheme {
    /** Colores propios del tema activo. */
    val colors: ApuntiaColors
        @Composable
        @ReadOnlyComposable
        get() = localApuntiaColors.current
}

private val darkScheme: ColorScheme = darkColorScheme(
    primary = mint,
    onPrimary = cardInk,
    primaryContainer = darkSurface,
    onPrimaryContainer = darkOnBackground,
    secondary = categoryLavender,
    onSecondary = cardInk,
    background = darkBackground,
    onBackground = darkOnBackground,
    surface = darkBackground,
    onSurface = darkOnBackground,
    surfaceVariant = darkSurface,
    onSurfaceVariant = darkOnSurfaceVariant,
    surfaceContainerLowest = darkBackground,
    surfaceContainerLow = darkBackground,
    surfaceContainer = darkSurface,
    surfaceContainerHigh = darkSurface,
    surfaceContainerHighest = darkSurface,
    outline = darkOutline,
    outlineVariant = darkOutlineVariant,
    error = darkError,
    onError = darkOnError,
)

private val lightScheme: ColorScheme = lightColorScheme(
    primary = mint,
    onPrimary = cardInk,
    primaryContainer = lightSurface,
    onPrimaryContainer = lightOnBackground,
    secondary = categoryLavender,
    onSecondary = cardInk,
    background = lightBackground,
    onBackground = lightOnBackground,
    surface = lightBackground,
    onSurface = lightOnBackground,
    surfaceVariant = lightSurface,
    onSurfaceVariant = lightOnSurfaceVariant,
    surfaceContainerLowest = lightBackground,
    surfaceContainerLow = lightBackground,
    surfaceContainer = lightSurface,
    surfaceContainerHigh = lightSurface,
    surfaceContainerHighest = lightSurface,
    outline = lightOutline,
    outlineVariant = lightOutlineVariant,
    error = lightError,
    onError = lightOnError,
)

/**
 * Tema de Apuntia ("Papel de colores"). Sigue el modo claro u oscuro del teléfono.
 * No usa colores dinámicos para que la app se vea igual en todos los teléfonos.
 */
@Composable
fun ApuntiaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(localApuntiaColors provides if (darkTheme) darkApuntiaColors else lightApuntiaColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkScheme else lightScheme,
            typography = apuntiaTypography,
            content = content,
        )
    }
}
