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
import com.example.fitgymconnect.ui.shared.ClassUiState
import com.example.fitgymconnect.ui.shared.ClassViewModel
import com.example.fitgymconnect.ui.shared.ExerciseDetailScreen
import com.example.fitgymconnect.ui.shared.ProfileViewModel
import com.example.fitgymconnect.ui.shared.RoutineDetailScreen
import com.example.fitgymconnect.ui.shared.RoutineViewModel
import com.example.fitgymconnect.ui.shared.RoutinesScreen

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun TrainerMainScreen(onLogout: () -> Unit) {
    val navController      = rememberNavController()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val classViewModel: ClassViewModel     = hiltViewModel()
    val agendaViewModel: TrainerAgendaViewModel = hiltViewModel()
    val routineViewModel: RoutineViewModel = hiltViewModel()

    val userId     by profileViewModel.userId.collectAsState(initial = null)
    val classState by classViewModel.state.collectAsState()

    LaunchedEffect(classState, userId) {
        if (classState is ClassUiState.Success && userId != null) {
            agendaViewModel.loadAgenda(
                (classState as ClassUiState.Success).classes,
                userId!!
            )
        }
    }

    val items = listOf(
        NavItem("inicio",  "Inicio",      Icons.Default.Home),
        NavItem("rutinas", "Mis Rutinas", Icons.Default.FitnessCenter),
        NavItem("clases",  "Mis Clases",  Icons.Default.CalendarMonth),
        NavItem("perfil",  "Perfil",      Icons.Default.Person),
    )

    val detailRoutes = setOf("rutina_detalle", "ejercicio_detalle")

    Scaffold(
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val current = backStack?.destination?.route
            if (current !in detailRoutes) {
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
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor        = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor     = MaterialTheme.colorScheme.primary,
                                unselectedIconColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "inicio", modifier = Modifier.padding(padding)) {
            composable("inicio") {
                TrainerHomeScreen(
                    agendaViewModel     = agendaViewModel,
                    onNavigateToClases  = {
                        navController.navigate("clases") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    onNavigateToRutinas = {
                        navController.navigate("rutinas") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
            composable("rutinas") {
                RoutinesScreen(
                    filterByUserId = userId,
                    title = "Mis Rutinas",
                    viewModel = routineViewModel,
                    onRoutineClick = { routine ->
                        routineViewModel.selectRoutine(routine)
                        navController.navigate("rutina_detalle")
                    }
                )
            }
            composable("rutina_detalle") {
                RoutineDetailScreen(
                    routineViewModel = routineViewModel,
                    onBack = { navController.popBackStack() },
                    onExerciseClick = { exercise ->
                        routineViewModel.selectExercise(exercise)
                        navController.navigate("ejercicio_detalle")
                    }
                )
            }
            composable("ejercicio_detalle") {
                ExerciseDetailScreen(
                    routineViewModel = routineViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("clases")  { TrainerAgendaScreen(agendaViewModel = agendaViewModel) }
            composable("perfil")  {
                TrainerProfileScreen(
                    onLogout = onLogout,
                    agendaViewModel = agendaViewModel
                )
            }
        }
    }
}
