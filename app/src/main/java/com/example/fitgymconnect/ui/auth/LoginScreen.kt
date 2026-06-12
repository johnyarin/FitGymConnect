package com.example.fitgymconnect.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.fitgymconnect.ui.theme.OrangeLight
import com.example.fitgymconnect.ui.theme.OrangePrimary

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onPendingVerification: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val emailError    = submitted && email.isBlank()
    val passwordError = submitted && password.isBlank()

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                val role = (uiState as AuthUiState.Success).role
                viewModel.resetState()
                onLoginSuccess(role)
            }
            is AuthUiState.PendingVerification -> {
                val pendingEmail = (uiState as AuthUiState.PendingVerification).email
                viewModel.resetState()
                onPendingVerification(pendingEmail)
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.White, OrangeLight, OrangePrimary))
        ).padding(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "FitGymConnect",
            contentScale = ContentScale.Fit,
            modifier = Modifier.width(420.dp).height(150.dp)
        )
        Spacer(Modifier.height(0.dp))
        Text("Inicia sesión en tu cuenta", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (uiState is AuthUiState.Error) viewModel.resetState()
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) ({ Text("Campo obligatorio") }) else null,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                errorContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (uiState is AuthUiState.Error) viewModel.resetState()
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = passwordError,
            supportingText = if (passwordError) ({ Text("Campo obligatorio") }) else null,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                errorContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        if (uiState is AuthUiState.Error) {
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                submitted = true
                if (email.isNotBlank() && password.isNotBlank()) {
                    viewModel.login(email.trim(), password)
                }
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Iniciar sesión")
            }
        }
        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
