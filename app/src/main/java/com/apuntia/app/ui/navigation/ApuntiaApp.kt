package com.apuntia.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apuntia.app.ApuntiaApplication
import com.apuntia.app.R
import com.apuntia.app.di.DatabaseStatus
import com.apuntia.app.domain.model.EntryType
import com.apuntia.app.ui.common.StartupErrorScreen
import com.apuntia.app.ui.components.ApuntiaDock
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.DockItem
import com.apuntia.app.ui.alarms.AlarmEditorScreen
import com.apuntia.app.ui.alarms.AlarmsScreen
import com.apuntia.app.ui.editor.EditorScreen
import com.apuntia.app.ui.notes.NotesScreen
import com.apuntia.app.ui.settings.SettingsScreen
import com.apuntia.app.ui.tasks.TasksScreen
import com.apuntia.app.ui.trash.TrashScreen

private val mainRoutes = listOf(Routes.NOTES, Routes.TASKS, Routes.ALARMS, Routes.SETTINGS)

/** Raíz de la interfaz: espera a que la base de datos esté lista y luego muestra la app. */
@Composable
fun ApuntiaApp() {
    val app = LocalContext.current.applicationContext as ApuntiaApplication
    val status by app.container.status.collectAsStateWithLifecycle()
    when (status) {
        // Mientras abre solo se ve el fondo; suele durar una fracción de segundo.
        DatabaseStatus.LOADING -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        DatabaseStatus.FAILED -> StartupErrorScreen(onRetry = app::openDatabase)
        DatabaseStatus.READY -> ApuntiaMainContent()
    }
}

@Composable
private fun ApuntiaMainContent() {
    val navController = rememberNavController()
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
    val selectedIndex = mainRoutes.indexOf(route)
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ApuntiaNavHost(navController)
        // La barra solo se ve en las cuatro pantallas principales, no en el editor.
        if (selectedIndex >= 0) {
            ApuntiaDock(
                items = dockItems(),
                selectedIndex = selectedIndex,
                onSelect = { navController.navigateToMain(mainRoutes[it]) },
                onAdd = {
                    val target = when (route) {
                        Routes.ALARMS -> Routes.alarmEditor()
                        Routes.TASKS -> Routes.editor(type = EntryType.TASK)
                        else -> Routes.editor()
                    }
                    navController.navigate(target) { launchSingleTop = true }
                },
                addDescription = stringResource(
                    when (route) {
                        Routes.ALARMS -> R.string.nav_new_alarm
                        Routes.TASKS -> R.string.nav_new_task
                        else -> R.string.nav_new_entry
                    },
                ),
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun dockItems() = listOf(
    DockItem(stringResource(R.string.nav_notes), ApuntiaIcons.notes),
    DockItem(stringResource(R.string.nav_tasks), ApuntiaIcons.tasks),
    DockItem(stringResource(R.string.nav_alarms), ApuntiaIcons.alarm),
    DockItem(stringResource(R.string.nav_settings), ApuntiaIcons.settings),
)

@Composable
private fun ApuntiaNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.NOTES) {
        composable(Routes.NOTES) {
            NotesScreen(
                onOpenEntry = { navController.navigate(Routes.editor(it)) { launchSingleTop = true } },
                onCreate = { navController.navigate(Routes.editor()) { launchSingleTop = true } },
                onOpenTrash = { navController.navigate(Routes.TRASH) { launchSingleTop = true } },
            )
        }
        composable(Routes.TRASH) { TrashScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.TASKS) {
            TasksScreen(
                onOpenEntry = { navController.navigate(Routes.editor(it)) { launchSingleTop = true } },
                onOpenTrash = { navController.navigate(Routes.TRASH) { launchSingleTop = true } },
            )
        }
        composable(Routes.ALARMS) {
            AlarmsScreen(onOpenAlarm = { navController.navigate(Routes.alarmEditor(it)) { launchSingleTop = true } })
        }
        composable(
            Routes.ALARM_EDITOR,
            arguments = listOf(
                navArgument(Routes.ALARM_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { AlarmEditorScreen(onClose = { navController.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen() }
        composable(
            Routes.EDITOR,
            arguments = listOf(
                navArgument(Routes.ENTRY_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Routes.TYPE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { EditorScreen(onClose = { navController.popBackStack() }) }
    }
}

/** Cambia de pantalla principal sin apilar copias y recordando el estado de cada una. */
private fun NavHostController.navigateToMain(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
