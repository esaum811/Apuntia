package com.apuntia.app.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta de Apuntia (ver docs/DOCUMENTACION_APUNTIA.md, sección "Diseño de la interfaz").
// Los nombres van en camelCase porque ktlint no acepta PascalCase en propiedades.

// ----- Modo claro -----
internal val lightBackground = Color(0xFFF6F9FC)
internal val lightSurface = Color(0xFFFFFFFF)
internal val lightSurfaceVariant = Color(0xFFEDF1F6)
internal val lightOutline = Color(0xFF8A97A8)
internal val lightOutlineVariant = Color(0xFFE3E9F0)
internal val lightOnSurface = Color(0xFF1F2A37)
internal val lightOnSurfaceVariant = Color(0xFF5B6776)

internal val lightBlue = Color(0xFF2F6DB5)
internal val lightOnBlue = Color(0xFFFFFFFF)
internal val lightBlueSoft = Color(0xFFE4EEF9)
internal val lightOnBlueSoft = Color(0xFF255C9E)

internal val lightOrange = Color(0xFFF0883A)
internal val lightOnOrange = Color(0xFF1F2A37)
internal val lightOrangeSoft = Color(0xFFFDEBDB)
internal val lightOnOrangeSoft = Color(0xFF9A4A0C)

internal val lightError = Color(0xFFB3261E)
internal val lightOnError = Color(0xFFFFFFFF)

// ----- Modo oscuro -----
internal val darkBackground = Color(0xFF0F1722)
internal val darkSurface = Color(0xFF182231)
internal val darkSurfaceVariant = Color(0xFF263447)
internal val darkOutline = Color(0xFF5B6B80)
internal val darkOutlineVariant = Color(0xFF263447)
internal val darkOnSurface = Color(0xFFE8EEF6)
internal val darkOnSurfaceVariant = Color(0xFF9AA8BA)

// El azul y el naranja se aclaran en oscuro para conservar el contraste.
internal val darkBlue = Color(0xFF6AA6E8)
internal val darkOnBlue = Color(0xFF0B1420)
internal val darkBlueSoft = Color(0xFF1F3552)
internal val darkOnBlueSoft = Color(0xFFA9CBF3)

internal val darkOrange = Color(0xFFF59A52)
internal val darkOnOrange = Color(0xFF1B1208)
internal val darkOrangeSoft = Color(0xFF3A2616)
internal val darkOnOrangeSoft = Color(0xFFF7BC8A)

internal val darkError = Color(0xFFF2B8B5)
internal val darkOnError = Color(0xFF601410)
