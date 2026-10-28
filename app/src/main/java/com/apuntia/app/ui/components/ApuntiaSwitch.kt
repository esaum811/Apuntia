package com.apuntia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.apuntia.app.ui.theme.ApuntiaTheme
import com.apuntia.app.ui.theme.cardInk

private val TrackWidth = 64.dp
private val TrackHeight = 38.dp
private val ThumbSize = 26.dp
private val ThumbInset = 6.dp

/**
 * Interruptor de Apuntia: encendido pista oscura con bolita menta; apagado, contorno con bolita gris.
 *
 * @param description nombre que leen los lectores de pantalla (por ejemplo "Alarma de las 7:00").
 */
@Composable
fun ApuntiaSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    // El área tocable mide 48 dp de alto aunque la pista dibujada sea más baja.
    Box(
        modifier
            .defaultMinSize(minWidth = TrackWidth, minHeight = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(TrackWidth, TrackHeight)
                .background(if (checked) cardInk else scheme.surfaceVariant, CircleShape)
                .border(2.dp, if (checked) cardInk else scheme.outline, CircleShape),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = if (checked) TrackWidth - ThumbSize - ThumbInset else ThumbInset)
                    .size(ThumbSize)
                    .background(if (checked) ApuntiaTheme.colors.accent else scheme.outline, CircleShape),
            )
        }
    }
}
