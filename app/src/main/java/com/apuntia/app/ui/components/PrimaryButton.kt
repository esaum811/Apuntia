package com.apuntia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.apuntia.app.ui.theme.ApuntiaTheme

/**
 * Botón principal de color menta, ancho completo. Úsalo para la acción más importante de la
 * pantalla: Siguiente, Guardar, Terminar.
 */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ApuntiaTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .background(colors.accent, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = colors.onAccent)
    }
}
