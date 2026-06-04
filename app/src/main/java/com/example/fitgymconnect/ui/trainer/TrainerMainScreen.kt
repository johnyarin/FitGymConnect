package com.example.fitgymconnect.ui.trainer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitgymconnect.ui.shared.ClassesScreen
import com.example.fitgymconnect.ui.shared.ProfileViewModel
import com.example.fitgymconnect.ui.shared.RoutinesScreen

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun TrainerMainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val userId by profileViewModel.userId.collectAsState(initial = null)

    val items = listOf(
        NavItem("inicio",  "Inicio",      Icons.Default.Home),
        NavItem("rutinas", "Mis Rutinas", Icons.Default.FitnessCenter),
        NavItem("clases",  "Mis Clases",  Icons.Default.CalendarMonth),
        NavItem("perfil",  "Perfil",      Icons.Default.Person),
    )

    Scaffold(
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val current = backStack?.destination?.route
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = current == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon  = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "inicio", modifier = Modifier.padding(padding)) {
            composable("inicio") {
                TrainerHomeScreen(
                    onNavigateToClases = { navController.navigate("clases") },
                    onNavigateToRutinas = { navController.navigate("rutinas") }
                )
            }
            composable("rutinas") { RoutinesScreen(filterByUserId = userId, title = "Mis Rutinas") }
            composable("clases")  { ClassesScreen(filterByUserId = userId, title = "Mis Clases") }
            composable("perfil")  { TrainerProfileScreen(onLogout = onLogout) }
        }
    }
}
