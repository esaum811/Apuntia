package com.apuntia.app.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apuntia.app.data.local.database.DefaultCategories
import com.apuntia.app.domain.model.CategoryShape
import com.apuntia.app.ui.components.ApuntiaDock
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.CategoryCard
import com.apuntia.app.ui.components.CategoryChip
import com.apuntia.app.ui.components.DockItem
import com.apuntia.app.ui.theme.ApuntiaTheme
import com.apuntia.app.ui.theme.cardInk
import com.apuntia.app.ui.theme.cardInkSoft
import androidx.compose.ui.graphics.Color

private val sampleNotes = listOf(
    Triple("Entrega del ensayo de historia", "Revisar la bibliografía y agregar las conclusiones.", "Hoy, 4:30 p. m."),
    Triple("Ideas para la reunión semanal", "Proponer horario fijo y definir responsables.", "Ayer"),
    Triple("Lista de compras", "Cuadernos, marcadores, pila para el reloj.", "12 oct"),
)

/**
 * Pantalla temporal para ver el diseño (tarjetas, filtros y barra inferior) con datos de ejemplo.
 * Se borra cuando existan las pantallas reales.
 */
@Composable
fun DesignPreviewScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableIntStateOf(-1) }
    val categories = DefaultCategories.all
    val items = listOf(
        DockItem("Notas", ApuntiaIcons.notes),
        DockItem("Tareas", ApuntiaIcons.tasks),
        DockItem("Alarmas", ApuntiaIcons.alarm),
        DockItem("Ajustes", ApuntiaIcons.settings),
    )
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Mis notas",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CategoryChip("Todas", selectedCategory == -1, { selectedCategory = -1 })
                categories.forEachIndexed { i, c ->
                    CategoryChip(
                        c.name, selectedCategory == i, { selectedCategory = i },
                        shape = CategoryShape.fromName(c.shape), color = Color(c.color),
                    )
                }
            }
            sampleNotes.forEachIndexed { i, (title, body, date) ->
                val c = categories[i]
                CategoryCard(c.name, CategoryShape.fromName(c.shape), Color(c.color), onClick = {}) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = cardInk)
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = cardInkSoft)
                    Text(date, style = MaterialTheme.typography.labelMedium, color = cardInk)
                }
            }
            Box(Modifier.padding(bottom = 120.dp))
        }
        ApuntiaDock(
            items, selectedTab, { selectedTab = it }, onAdd = {}, addDescription = "Nueva nota",
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
        )
    }
}

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DesignPreviewScreenPreview() {
    ApuntiaTheme { DesignPreviewScreen() }
}
