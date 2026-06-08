package com.example.fitgymconnect.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitgymconnect.ui.auth.LoginScreen
import com.example.fitgymconnect.ui.auth.RegisterScreen
import com.example.fitgymconnect.ui.main.AuthState
import com.example.fitgymconnect.ui.theme.FitGymConnectTheme
import com.example.fitgymconnect.ui.main.MainViewModel
import com.example.fitgymconnect.ui.student.StudentMainScreen
import com.example.fitgymconnect.ui.trainer.TrainerMainScreen

private object Routes {
    const val SPLASH   = "splash"
    const val LOGIN    = "login"
    const val REGISTER = "register"
    const val STUDENT  = "student"
    const val TRAINER  = "trainer"
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val authState by mainViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Loading -> {}
                    is AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                    is AuthState.Authenticated -> {
                        val dest = if ((authState as AuthState.Authenticated).role == "trainer")
                            Routes.TRAINER else Routes.STUDENT
                        navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    }
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable(Routes.LOGIN) {
            FitGymConnectTheme(darkTheme = false) {
                LoginScreen(
                    onLoginSuccess = { role ->
                        val dest = if (role == "trainer") Routes.TRAINER else Routes.STUDENT
                        navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
        }

        composable(Routes.REGISTER) {
            FitGymConnectTheme(darkTheme = false) {
                RegisterScreen(
                    onRegisterSuccess = { role ->
                        val dest = if (role == "trainer") Routes.TRAINER else Routes.STUDENT
                        navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.STUDENT) {
            StudentMainScreen(
                onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(Routes.TRAINER) {
            TrainerMainScreen(
                onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }
    }
}
