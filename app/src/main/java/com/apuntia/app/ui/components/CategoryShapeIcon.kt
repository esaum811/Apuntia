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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apuntia.app.domain.model.CategoryShape

private const val SQUARE_INSET = 1.5f / 14f
private const val SQUARE_RADIUS = 2f / 14f
private const val OUTLINE_WIDTH = 1.5f / 14f

/**
 * Dibuja la forma de una categoría (círculo, cuadrado o rombo) del [color] indicado.
 * Es decorativa: el nombre de la categoría siempre aparece junto a ella.
 *
 * @param outline color del borde; déjalo sin especificar para no dibujar borde. Sirve para que la
 * forma se vea sobre fondos claros.
 */
@Composable
fun CategoryShapeIcon(
    shape: CategoryShape,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    outline: Color = Color.Unspecified,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        drawShape(shape, color, w, null)
        if (outline != Color.Unspecified) drawShape(shape, outline, w, Stroke(w * OUTLINE_WIDTH))
    }
}

private fun DrawScope.drawShape(shape: CategoryShape, color: Color, w: Float, stroke: Stroke?) {
    val style = stroke ?: Fill
    when (shape) {
        CategoryShape.CIRCLE -> drawCircle(color, style = style)
        CategoryShape.SQUARE -> drawRoundRect(
            color = color,
            topLeft = Offset(w * SQUARE_INSET, w * SQUARE_INSET),
            size = Size(w * (1 - 2 * SQUARE_INSET), w * (1 - 2 * SQUARE_INSET)),
            cornerRadius = CornerRadius(w * SQUARE_RADIUS),
            style = style,
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
            style = style,
        )
    }
}
