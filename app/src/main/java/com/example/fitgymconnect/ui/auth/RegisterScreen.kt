package com.example.fitgymconnect.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var submitted       by remember { mutableStateOf(false) }

    val nameError            = submitted && name.isBlank()
    val emailError           = submitted && email.isBlank()
    val emailFormatError     = submitted && email.isNotBlank() && !email.contains("@")
    val passwordError        = submitted && password.isBlank()
    val passwordShortError   = submitted && password.isNotBlank() && password.length < 8
    val confirmBlankError    = submitted && confirmPassword.isBlank()
    val confirmMismatchError = submitted && confirmPassword.isNotBlank() && confirmPassword != password

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val role = (uiState as AuthUiState.Success).role
            viewModel.resetState()
            onRegisterSuccess(role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (uiState is AuthUiState.Error) viewModel.resetState()
            },
            label = { Text("Nombre completo") },
            singleLine = true,
            isError = nameError,
            supportingText = if (nameError) ({ Text("Campo obligatorio") }) else null,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (uiState is AuthUiState.Error) viewModel.resetState()
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailError || emailFormatError,
            supportingText = when {
                emailError       -> ({ Text("Campo obligatorio") })
                emailFormatError -> ({ Text("Introduce un email válido") })
                else             -> null
            },
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
            isError = passwordError || passwordShortError,
            supportingText = when {
                passwordError      -> ({ Text("Campo obligatorio") })
                passwordShortError -> ({ Text("Mínimo 8 caracteres") })
                else               -> null
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = confirmBlankError || confirmMismatchError,
            supportingText = when {
                confirmBlankError    -> ({ Text("Campo obligatorio") })
                confirmMismatchError -> ({ Text("Las contraseñas no coinciden") })
                else                 -> null
            },
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
                val valid = name.isNotBlank()
                    && email.isNotBlank() && email.contains("@")
                    && password.isNotBlank() && password.length >= 8
                    && confirmPassword == password
                if (valid) {
                    viewModel.register(name.trim(), email.trim(), password)
                }
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Crear cuenta")
            }
        }
        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
