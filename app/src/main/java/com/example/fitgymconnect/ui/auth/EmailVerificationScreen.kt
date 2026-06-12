package com.example.fitgymconnect.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.R
import com.example.fitgymconnect.ui.theme.OrangeLight
import com.example.fitgymconnect.ui.theme.OrangePrimary

@Composable
fun EmailVerificationScreen(
    email: String,
    onVerified: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val resendState by viewModel.resendState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            viewModel.resetState()
            onVerified((uiState as AuthUiState.Success).role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, OrangeLight, OrangePrimary)))
            .padding(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "FitGymConnect",
            contentScale = ContentScale.Fit,
            modifier = Modifier.width(220.dp).height(100.dp)
        )
        Spacer(Modifier.height(32.dp))

        Text(
            text = "Verifica tu email",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Hemos enviado un enlace de verificación a:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Revisa también la carpeta de spam.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        // Un único mensaje a la vez: resend tiene prioridad si está activo
        val feedbackMessage: String?
        val feedbackIsError: Boolean
        when {
            resendState is ResendState.Done -> {
                feedbackMessage = (resendState as ResendState.Done).message
                feedbackIsError = !(resendState as ResendState.Done).success
            }
            uiState is AuthUiState.Error -> {
                feedbackMessage = (uiState as AuthUiState.Error).message
                feedbackIsError = true
            }
            else -> {
                feedbackMessage = null
                feedbackIsError = false
            }
        }
        if (feedbackMessage != null) {
            Text(
                text = feedbackMessage,
                color = if (feedbackIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.resetResendState()
                viewModel.resetState()
                viewModel.checkVerification()
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Ya verifiqué mi cuenta")
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                viewModel.resetState()
                viewModel.resetResendState()
                viewModel.resendVerification()
            },
            enabled = resendState !is ResendState.Loading,
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (resendState is ResendState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Reenviar email")
            }
        }
        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Volver al inicio de sesión")
        }
    }
}
