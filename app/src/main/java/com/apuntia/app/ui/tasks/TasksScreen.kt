package com.apuntia.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntia.app.R
import com.apuntia.app.domain.model.TaskSectionType
import com.apuntia.app.ui.AppViewModelProvider
import com.apuntia.app.ui.common.dueDateLabel
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.CategoryShapeIcon
import com.apuntia.app.ui.theme.ApuntiaTheme
import com.apuntia.app.ui.theme.cardInk

// Espacio al final de la lista para que la barra inferior no tape la última tarea.
private val DockClearance = 130.dp

/**
 * Pantalla de tareas: pendientes agrupadas por fecha límite y hechas.
 *
 * @param onOpenEntry se llama con el id de la tarea tocada.
 */
@Composable
fun TasksScreen(
    onOpenEntry: (String) -> Unit,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = DockClearance + navigationBarHeight,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "title") {
            Text(
                stringResource(R.string.tasks_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        item(key = "tabs") { TaskTabs(state, viewModel::selectTab) }
        if (state.showDone) {
            doneContent(state, onOpenEntry, viewModel::setDone)
        } else {
            pendingContent(state, onOpenEntry, viewModel::setDone)
        }
    }
}

private fun LazyListScope.pendingContent(
    state: TasksUiState,
    onOpen: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
) {
    if (!state.isLoading && state.pendingGroups.isEmpty()) {
        item(key = "empty-pending") {
            EmptyTasks(
                stringResource(R.string.tasks_empty_pending_title),
                stringResource(R.string.tasks_empty_pending_hint),
            )
        }
    }
    state.pendingGroups.forEach { group ->
        item(key = "header-${group.type}") { SectionHeader(stringResource(group.type.titleRes())) }
        items(group.items, key = { it.entry.id }, contentType = { "task" }) { TaskRow(it, onOpen, onToggle) }
    }
}

private fun LazyListScope.doneContent(
    state: TasksUiState,
    onOpen: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
) {
    if (!state.isLoading && state.done.isEmpty()) {
        item(key = "empty-done") {
            EmptyTasks(
                stringResource(R.string.tasks_empty_done_title),
                stringResource(R.string.tasks_empty_done_hint),
            )
        }
    }
    items(state.done, key = { it.entry.id }, contentType = { "task" }) { TaskRow(it, onOpen, onToggle) }
}

private fun TaskSectionType.titleRes(): Int = when (this) {
    TaskSectionType.OVERDUE -> R.string.tasks_overdue
    TaskSectionType.TODAY -> R.string.tasks_today
    TaskSectionType.THIS_WEEK -> R.string.tasks_this_week
    TaskSectionType.LATER -> R.string.tasks_later
    TaskSectionType.NO_DATE -> R.string.tasks_no_date
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun TaskTabs(state: TasksUiState, onSelect: (Boolean) -> Unit) {
    Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TabChip(stringResource(R.string.tasks_tab_pending), state.pendingCount, !state.showDone) { onSelect(false) }
        TabChip(stringResource(R.string.tasks_tab_done), state.done.size, state.showDone) { onSelect(true) }
    }
}

@Composable
private fun TabChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .defaultMinSize(minHeight = 48.dp)
            .background(if (selected) scheme.onBackground else scheme.surfaceVariant, RoundedCornerShape(14.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) scheme.background else scheme.onBackground,
        )
        Box(
            Modifier
                .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
                .background(if (selected) scheme.background else scheme.surface, RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = scheme.onBackground)
        }
    }
}

@Composable
private fun TaskRow(item: TaskItem, onOpen: (String) -> Unit, onToggle: (String, Boolean) -> Unit) {
    val entry = item.entry
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceVariant, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button) { onOpen(entry.id) }
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskCheckbox(entry.title, entry.isDone) { onToggle(entry.id, it) }
        Column(Modifier.weight(1f).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                entry.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (entry.isDone) TextDecoration.LineThrough else null,
                ),
                color = if (entry.isDone) scheme.onSurfaceVariant else scheme.onBackground,
            )
            if (!entry.isDone) TaskMeta(item)
        }
    }
}

@Composable
private fun TaskMeta(item: TaskItem) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoryShapeIcon(item.category.shape, Color(item.category.color), outline = scheme.onBackground)
        Text(item.category.name, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        item.entry.dueAt?.let {
            Text(dueDateLabel(it), style = MaterialTheme.typography.labelMedium, color = scheme.onBackground)
        }
    }
}

@Composable
private fun TaskCheckbox(title: String, isDone: Boolean, onChange: (Boolean) -> Unit) {
    val colors = ApuntiaTheme.colors
    val label = stringResource(if (isDone) R.string.tasks_mark_pending else R.string.tasks_mark_done, title)
    Box(
        Modifier
            .size(48.dp)
            .semantics { contentDescription = label }
            .toggleable(value = isDone, role = Role.Checkbox, onValueChange = onChange),
        contentAlignment = Alignment.Center,
    ) {
        val box = Modifier.size(30.dp)
        if (isDone) {
            Box(box.background(cardInk, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(
                    ApuntiaIcons.check,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Box(box.border(3.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)))
        }
    }
}

@Composable
private fun EmptyTasks(title: String, hint: String) {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(hint, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
