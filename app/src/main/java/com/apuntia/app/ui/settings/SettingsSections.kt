package com.apuntia.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apuntia.app.R
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.theme.ApuntiaTheme

// Amarillo de los avisos "Falta permitir"; es el mismo de las categorías.
private val WarningBadge = Color(0xFFFFD84D)
private val GroupShape = RoundedCornerShape(22.dp)
private val ValueMaxWidth = 150.dp

/** Título de una sección de Ajustes (Seguridad, Alarmas...). */
@Composable
internal fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

/** Tarjeta que agrupa varias filas de una misma sección. */
@Composable
internal fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(GroupShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        content = content,
    )
}

/** Línea fina entre dos filas de un mismo grupo. */
@Composable
internal fun SettingsDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

/**
 * Fila de Ajustes: título a la izquierda y, a la derecha, un valor o una etiqueta de estado.
 *
 * @param title nombre de la opción.
 * @param onClick se llama al tocar la fila; con nulo la fila no se toca.
 * @param value texto pequeño con el valor actual.
 * @param badge etiqueta de estado, por ejemplo "Activado"; el segundo dato dice si es buena noticia.
 * @param showChevron muestra la flecha que indica que la fila abre otra cosa.
 * @param maxLines líneas máximas del título; si no cabe, se corta con "…".
 * @param horizontalPadding espacio a los lados de la fila.
 */
@Composable
internal fun SettingsRow(
    title: String,
    onClick: (() -> Unit)?,
    value: String? = null,
    badge: Pair<String, Boolean>? = null,
    showChevron: Boolean = true,
    maxLines: Int = 1,
    horizontalPadding: Dp = 16.dp,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 58.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onBackground,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
        if (value != null) {
            Text(
                value,
                modifier = Modifier.widthIn(max = ValueMaxWidth),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (badge != null) StatusBadge(badge.first, good = badge.second)
        if (showChevron && onClick != null) {
            Icon(ApuntiaIcons.chevron, contentDescription = null, Modifier.size(22.dp), tint = scheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusBadge(text: String, good: Boolean) {
    Text(
        text,
        modifier = Modifier
            .background(if (good) ApuntiaTheme.colors.accent else WarningBadge, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 3.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = ApuntiaTheme.colors.onAccent,
        maxLines = 1,
    )
}

/** Texto pequeño de ayuda debajo de un grupo. */
@Composable
internal fun SettingsCaption(text: String) {
    Text(
        text,
        modifier = Modifier.padding(horizontal = 4.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Diálogo con una lista de opciones de las que se elige una; al tocarla se aplica y se cierra. */
@Composable
internal fun ChoiceDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    message: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.selectableGroup()) {
                if (message != null) {
                    Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                }
                options.forEachIndexed { index, label ->
                    OptionRow(label, selected = index == selectedIndex) { onSelect(index) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}

/** Fila con botón de opción, usada en las listas de los diálogos. */
@Composable
internal fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
