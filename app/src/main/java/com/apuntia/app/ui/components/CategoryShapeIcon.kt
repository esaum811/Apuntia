package com.apuntia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apuntia.app.domain.model.CategoryShape

private const val SQUARE_INSET = 1.5f / 14f
private const val SQUARE_RADIUS = 2f / 14f

/**
 * Dibuja la forma de una categoría (círculo, cuadrado o rombo) del [color] indicado.
 * Es decorativa: el nombre de la categoría siempre aparece junto a ella.
 */
@Composable
fun CategoryShapeIcon(shape: CategoryShape, color: Color, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        when (shape) {
            CategoryShape.CIRCLE -> drawCircle(color)
            CategoryShape.SQUARE -> drawRoundRect(
                color = color,
                topLeft = Offset(w * SQUARE_INSET, w * SQUARE_INSET),
                size = Size(w * (1 - 2 * SQUARE_INSET), w * (1 - 2 * SQUARE_INSET)),
                cornerRadius = CornerRadius(w * SQUARE_RADIUS),
            )
            CategoryShape.DIAMOND -> drawPath(
                Path().apply {
                    moveTo(w / 2, 0f)
                    lineTo(w, w / 2)
                    lineTo(w / 2, w)
                    lineTo(0f, w / 2)
                    close()
                },
                color,
            )
        }
    }
}
