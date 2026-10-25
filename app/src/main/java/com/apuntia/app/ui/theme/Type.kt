package com.apuntia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.apuntia.app.R

private const val WIDTH_CONDENSED = 75f
private const val WIDTH_NORMAL = 100f

/** Crea una variante de la fuente variable con el peso y el ancho indicados. */
@OptIn(ExperimentalTextApi::class)
private fun bricolage(weight: FontWeight, width: Float): Font = Font(
    resId = R.font.bricolage_grotesque,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.width(width),
    ),
)

// Títulos: letras condensadas y muy gruesas.
private val displayFamily = FontFamily(
    bricolage(FontWeight.Bold, WIDTH_CONDENSED),
    bricolage(FontWeight.ExtraBold, WIDTH_CONDENSED),
)

// Texto corrido: ancho normal.
private val textFamily = FontFamily(
    bricolage(FontWeight.Medium, WIDTH_NORMAL),
    bricolage(FontWeight.Bold, WIDTH_NORMAL),
    bricolage(FontWeight.ExtraBold, WIDTH_NORMAL),
)

/** Escala tipográfica de Apuntia, tomada de los bocetos "Papel de colores". */
internal val apuntiaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 60.sp, lineHeight = 54.sp, letterSpacing = (-0.02).em,
    ),
    headlineLarge = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 46.sp, lineHeight = 45.sp, letterSpacing = (-0.015).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 38.sp, lineHeight = 38.sp, letterSpacing = (-0.015).em,
    ),
    titleLarge = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em,
    ),
    titleMedium = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp, lineHeight = 25.sp, letterSpacing = (-0.01).em,
    ),
    bodyLarge = TextStyle(
        fontFamily = textFamily, fontWeight = FontWeight.Medium,
        fontSize = 17.sp, lineHeight = 26.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = textFamily, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 21.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = textFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp, lineHeight = 24.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = textFamily, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = textFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
)

/** Estilo de la hora gigante de la pantalla de alarma. */
val alarmTimeStyle: TextStyle = TextStyle(
    fontFamily = displayFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 168.sp,
    lineHeight = 134.sp,
    letterSpacing = (-0.03).em,
)
