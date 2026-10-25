package com.apuntia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

private val TabShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
private val BodyShape = RoundedCornerShape(topStart = 0.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 22.dp)

/**
 * Tarjeta con forma de carpeta: una pestaña con la categoría pegada al cuerpo de color.
 * El texto de adentro siempre es oscuro porque los colores de categoría son claros.
 *
 * @param categoryName nombre que se escribe en la pestaña.
 * @param shape forma de la categoría.
 * @param color color de la categoría.
 * @param onClick acción al tocar la tarjeta.
 * @param content contenido del cuerpo (título, texto, fecha).
 */
@Composable
fun CategoryCard(
    categoryName: String,
    shape: CategoryShape,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)) {
        Row(
            Modifier.background(color, TabShape).padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CategoryShapeIcon(shape, cardInk)
            Text(categoryName, style = MaterialTheme.typography.labelMedium, color = cardInk)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(color, BodyShape)
                .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}
