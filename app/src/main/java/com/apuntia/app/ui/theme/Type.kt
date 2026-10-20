package com.apuntia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.apuntia.app.R

/**
 * Familia tipográfica Nunito, incluida dentro de la app (res/font) para que
 * funcione sin conexión a internet.
 */
internal val nunitoFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

private val baseTypography = Typography()

/** Aplica Nunito a un estilo de texto de Material sin cambiar el resto de sus valores. */
private fun TextStyle.withNunito(): TextStyle = copy(fontFamily = nunitoFamily)

/**
 * Tipografía de Apuntia.
 *
 * Los estilos que se usan en el diseño aprobado están definidos a mano; los demás
 * conservan las medidas de Material 3 pero con la fuente Nunito.
 */
internal val apuntiaTypography = Typography(
    displayLarge = baseTypography.displayLarge.withNunito(),
    displayMedium = baseTypography.displayMedium.withNunito(),
    displaySmall = baseTypography.displaySmall.withNunito(),
    // Títulos de pantalla ("Mis notas", "Tareas")
    headlineLarge = TextStyle(
        fontFamily = nunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = baseTypography.headlineMedium.withNunito(),
    headlineSmall = baseTypography.headlineSmall.withNunito(),
    // Título de la barra superior y de las tarjetas grandes
    titleLarge = TextStyle(
        fontFamily = nunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    // Título de cada nota o tarea en la lista
    titleMedium = TextStyle(
        fontFamily = nunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = baseTypography.titleSmall.withNunito(),
    // Texto de las notas
    bodyLarge = TextStyle(
        fontFamily = nunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
    ),
    // Vista previa y textos secundarios
    bodyMedium = TextStyle(
        fontFamily = nunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = baseTypography.bodySmall.withNunito(),
    // Botones
    labelLarge = TextStyle(
        fontFamily = nunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    // Chips de categoría y fechas
    labelMedium = TextStyle(
        fontFamily = nunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
    // Etiquetas de la barra inferior
    labelSmall = TextStyle(
        fontFamily = nunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
