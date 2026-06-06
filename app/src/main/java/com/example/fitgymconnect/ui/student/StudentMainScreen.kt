package com.example.fitgymconnect.ui.student

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
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
import com.example.fitgymconnect.ui.shared.RoutinesScreen

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun StudentMainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val bookingViewModel: BookingViewModel = hiltViewModel()
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()

    val subState by subscriptionViewModel.state.collectAsState()
    val hasActiveSub = subState is SubscriptionUiState.Active

    val items = listOf(
        NavItem("inicio",   "Inicio",   Icons.Default.Home),
        NavItem("rutinas",  "Rutinas",  Icons.Default.FitnessCenter),
        NavItem("clases",   "Clases",   Icons.Default.CalendarMonth),
        NavItem("reservas", "Reservas", Icons.Default.Bookmark),
        NavItem("perfil",   "Perfil",   Icons.Default.Person),
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
                StudentHomeScreen(
                    bookingViewModel = bookingViewModel,
                    subscriptionViewModel = subscriptionViewModel,
                    onNavigateToClases = { navController.navigate("clases") },
                    onNavigateToRutinas = { navController.navigate("rutinas") },
                    onNavigateToPerfil = { navController.navigate("perfil") }
                )
            }
            composable("rutinas")  { RoutinesScreen(hasActiveSubscription = hasActiveSub) }
            composable("clases") {
                ClassesScreen(
                    showBookingButton = true,
                    hasActiveSubscription = hasActiveSub,
                    bookingViewModel = bookingViewModel
                )
            }
            composable("reservas") { MyBookingsScreen(viewModel = bookingViewModel) }
            composable("perfil") {
                StudentProfileScreen(
                    onLogout = onLogout,
                    subscriptionViewModel = subscriptionViewModel,
                    bookingViewModel = bookingViewModel
                )
            }
        }
    }
}
