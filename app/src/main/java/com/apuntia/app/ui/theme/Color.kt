package com.apuntia.app.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta "Papel de colores". Los colores de categoría y la tinta de las tarjetas son iguales
// en modo claro y oscuro; solo cambian el fondo y las superficies.

// Tinta: texto sobre las tarjetas de color. Es oscura en los dos modos.
internal val cardInk = Color(0xFF1C1A3D)
internal val cardInkSoft = Color(0xFF322E63)

// Acción principal (Guardar, Terminar, botón "+").
internal val mint = Color(0xFF62E6B7)

// Categorías por defecto.
internal val categoryYellow = Color(0xFFFFD84D)
internal val categoryLavender = Color(0xFF8FA8FF)
internal val categoryPink = Color(0xFFFF9DBB)

// Modo oscuro.
internal val darkBackground = Color(0xFF1C1A3D)
internal val darkSurface = Color(0xFF2B2866)
internal val darkOnBackground = Color(0xFFFAF9FC)
internal val darkOnSurfaceVariant = Color(0xFFC4C0EA)
internal val darkOutline = Color(0xFF8A85D6)
internal val darkOutlineVariant = Color(0xFF6A65B8)
internal val darkError = Color(0xFFFF8A80)
internal val darkOnError = Color(0xFF1C1A3D)

// Modo claro.
internal val lightBackground = Color(0xFFF5F4F9)
internal val lightSurface = Color(0xFFE8E5F4)
internal val lightOnBackground = Color(0xFF1C1A3D)
internal val lightOnSurfaceVariant = Color(0xFF4E4A85)
internal val lightOutline = Color(0xFF6A65B8)
internal val lightOutlineVariant = Color(0xFFC4C0EA)
internal val lightError = Color(0xFFB3261E)
internal val lightOnError = Color(0xFFFFFFFF)

// Barra inferior flotante.
internal val dockSelected = Color(0xFFFAF9FC)
internal val dockOnSelected = Color(0xFF1C1A3D)
internal val dockMuted = Color(0xFFC4C0EA)
