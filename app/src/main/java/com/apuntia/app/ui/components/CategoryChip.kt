package com.apuntia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.apuntia.app.domain.model.CategoryShape
import com.apuntia.app.ui.theme.cardInk

private val ChipShape = RoundedCornerShape(14.dp)

/**
 * Botón de filtro o de selección de categoría.
 * Seleccionado se rellena con el color de la categoría; sin seleccionar queda en la superficie
 * normal con la forma de la categoría a un lado.
 *
 * @param shape forma de la categoría, o nulo para el chip "Todas".
 * @param color color de la categoría; se ignora si [shape] es nulo.
 */
@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: CategoryShape? = null,
    color: Color = Color.Unspecified,
) {
    val scheme = MaterialTheme.colorScheme
    val filled = selected && shape != null
    val background = when {
        filled -> color
        selected -> scheme.onBackground
        else -> scheme.surfaceVariant
    }
    val textColor = when {
        filled -> cardInk
        selected -> scheme.background
        else -> scheme.onBackground
    }
    Row(
        modifier
            .defaultMinSize(minHeight = 44.dp)
            .background(background, ChipShape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (shape != null) CategoryShapeIcon(shape, if (filled) cardInk else color)
        Text(label, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}
