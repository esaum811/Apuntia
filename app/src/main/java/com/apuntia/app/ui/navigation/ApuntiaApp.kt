package com.apuntia.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apuntia.app.R
import com.apuntia.app.ui.common.ComingSoonScreen
import com.apuntia.app.ui.components.ApuntiaDock
import com.apuntia.app.ui.components.ApuntiaIcons
import com.apuntia.app.ui.components.DockItem
import com.apuntia.app.ui.editor.EditorScreen
import com.apuntia.app.ui.notes.NotesScreen

private val mainRoutes = listOf(Routes.NOTES, Routes.TASKS, Routes.ALARMS, Routes.SETTINGS)

/** Raíz de la interfaz: pantallas, navegación y barra inferior. */
@Composable
fun ApuntiaApp() {
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
                onAdd = { navController.navigate(Routes.editor()) },
                addDescription = stringResource(R.string.nav_new_entry),
                modifier = Modifier.align(Alignment.BottomCenter),
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
                onOpenEntry = { navController.navigate(Routes.editor(it)) },
                onCreate = { navController.navigate(Routes.editor()) },
            )
        }
        composable(Routes.TASKS) { ComingSoonScreen(stringResource(R.string.nav_tasks)) }
        composable(Routes.ALARMS) { ComingSoonScreen(stringResource(R.string.nav_alarms)) }
        composable(Routes.SETTINGS) { ComingSoonScreen(stringResource(R.string.nav_settings)) }
        composable(
            Routes.EDITOR,
            arguments = listOf(
                navArgument(Routes.ENTRY_ID_ARG) {
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
