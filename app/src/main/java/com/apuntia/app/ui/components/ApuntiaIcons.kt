package com.apuntia.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private const val ICON_SIZE = 24f
private const val STROKE_NORMAL = 2f
private const val STROKE_BOLD = 2.4f
private const val STROKE_EXTRA = 2.8f

/** Crea un ícono de líneas de 24x24 a partir de trazados SVG; el color lo pone quien lo usa. */
private fun strokeIcon(name: String, path: String, strokeWidth: Float = STROKE_NORMAL): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = ICON_SIZE.dp,
        defaultHeight = ICON_SIZE.dp,
        viewportWidth = ICON_SIZE,
        viewportHeight = ICON_SIZE,
    ).addPath(
        pathData = addPathNodes(path),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()

/** Íconos de Apuntia. Se crean una sola vez, la primera vez que se usan. */
object ApuntiaIcons {
    /** Notas. */
    val notes: ImageVector by lazy { strokeIcon("notes", "M7 3h7l4 4v14H7z M14 3v4h4 M10 12h5 M10 16h5") }

    /** Tareas. */
    val tasks: ImageVector by lazy {
        strokeIcon("tasks", "M7 4h10a3 3 0 0 1 3 3v10a3 3 0 0 1-3 3H7a3 3 0 0 1-3-3V7a3 3 0 0 1 3-3z M8 12l3 3 5-6")
    }

    /** Alarmas. */
    val alarm: ImageVector by lazy { strokeIcon("alarm", "M6 16V11a6 6 0 0 1 12 0v5l1.5 2h-15z M10 21h4") }

    /** Ajustes. */
    val settings: ImageVector by lazy {
        strokeIcon(
            "settings",
            "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6z M12 3v3 M12 18v3 M3 12h3 M18 12h3 " +
                "M5.6 5.6l2.1 2.1 M16.3 16.3l2.1 2.1 M18.4 5.6l-2.1 2.1 M7.7 16.3l-2.1 2.1",
        )
    }

    /** Buscar. */
    val search: ImageVector by lazy {
        strokeIcon("search", "M11 4a7 7 0 1 0 0 14a7 7 0 1 0 0-14z M20 20l-3.5-3.5", 2.2f)
    }

    /** Candado de las notas confidenciales. */
    val lock: ImageVector by lazy {
        strokeIcon(
            "lock",
            "M7 11h10a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2z " +
                "M8 11V8a4 4 0 0 1 8 0v3",
            STROKE_BOLD,
        )
    }

    /** Más (botón de nueva nota). */
    val plus: ImageVector by lazy { strokeIcon("plus", "M12 5v14 M5 12h14", STROKE_EXTRA) }

    /** Flecha para volver. */
    val back: ImageVector by lazy { strokeIcon("back", "M15 5l-7 7 7 7", STROKE_BOLD) }

    /** Palomita. */
    val check: ImageVector by lazy { strokeIcon("check", "M5 12.5l4.5 4.5L19 7.5", 3f) }

    /** Flecha hacia la derecha. */
    val chevron: ImageVector by lazy { strokeIcon("chevron", "M9 5l7 7-7 7", STROKE_BOLD) }
}
