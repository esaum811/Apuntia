package com.apuntia.app.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Un destino de la barra inferior.
 *
 * @property label texto que se muestra bajo el ícono.
 * @property icon ícono del destino.
 */
data class DockItem(val label: String, val icon: ImageVector)
