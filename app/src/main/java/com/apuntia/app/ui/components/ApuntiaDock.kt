package com.apuntia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.apuntia.app.ui.theme.ApuntiaTheme

private val DockShape = RoundedCornerShape(30.dp)
private val ItemShape = RoundedCornerShape(22.dp)

/**
 * Barra inferior flotante con cuatro destinos y el botón "+" incrustado al centro.
 *
 * @param items exactamente cuatro destinos: los dos primeros van a la izquierda y los dos últimos a la derecha.
 * @param selectedIndex posición del destino activo.
 * @param onSelect se llama con la posición del destino tocado.
 * @param onAdd se llama al tocar el botón "+".
 * @param addDescription texto que leen los lectores de pantalla para el botón "+".
 */
@Composable
fun ApuntiaDock(
    items: List<DockItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAdd: () -> Unit,
    addDescription: String,
    modifier: Modifier = Modifier,
) {
    require(items.size == 4) { "La barra necesita exactamente cuatro destinos" }
    val colors = ApuntiaTheme.colors
    // La caja es más alta que la barra para que el botón "+" quede dentro de sus límites y reciba toques.
    Box(modifier.fillMaxWidth().height(98.dp).padding(horizontal = 16.dp)) {
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp)
                .background(colors.dock, DockShape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.take(2).forEachIndexed { i, item -> DockButton(item, i == selectedIndex) { onSelect(i) } }
            Spacer(Modifier.width(88.dp))
            items.drop(2).forEachIndexed { i, item -> DockButton(item, i + 2 == selectedIndex) { onSelect(i + 2) } }
        }
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(68.dp)
                .background(colors.accent, CircleShape)
                .border(6.dp, MaterialTheme.colorScheme.background, CircleShape)
                .clickable(role = Role.Button, onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                ApuntiaIcons.plus,
                contentDescription = addDescription,
                tint = colors.onAccent,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun RowScope.DockButton(item: DockItem, selected: Boolean, onClick: () -> Unit) {
    val colors = ApuntiaTheme.colors
    val content = if (selected) colors.onDockSelected else colors.dockMuted
    Column(
        Modifier
            .weight(1f)
            .height(56.dp)
            .background(if (selected) colors.dockSelected else colors.dock, ItemShape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(item.icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        Text(item.label, style = MaterialTheme.typography.labelSmall, color = content)
    }
}
