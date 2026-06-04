package com.example.fitgymconnect.ui.student

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.ui.shared.ProfileViewModel
import com.example.fitgymconnect.utils.formatEndDate
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

@Composable
fun StudentProfileScreen(
    onLogout: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel(),
    bookingViewModel: BookingViewModel = hiltViewModel()
) {
    val userName  by profileViewModel.userName.collectAsState(initial = null)
    val userEmail by profileViewModel.userEmail.collectAsState(initial = null)
    val subState  by subscriptionViewModel.state.collectAsState()
    val isLoading by subscriptionViewModel.isLoading.collectAsState()
    val clientSecret by subscriptionViewModel.clientSecret.collectAsState()
    val message by subscriptionViewModel.message.collectAsState()
    val bookingState by bookingViewModel.state.collectAsState()
    val context = LocalContext.current

    val activeBookingsCount = when (bookingState) {
        is BookingUiState.Success -> (bookingState as BookingUiState.Success).bookings.count { it.status != "cancelled" }
        else -> 0
    }

    var capturedSecret by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> capturedSecret?.let { subscriptionViewModel.onPaymentSuccess(it) }
            is PaymentSheetResult.Canceled  -> subscriptionViewModel.onPaymentCanceled()
            is PaymentSheetResult.Failed    -> subscriptionViewModel.onPaymentFailed(result.error.message ?: "Error")
        }
    }

    LaunchedEffect(clientSecret) {
        clientSecret?.let { secret ->
            capturedSecret = secret
            paymentSheet.presentWithPaymentIntent(
                secret,
                PaymentSheet.Configuration(merchantDisplayName = "FitGymConnect")
            )
        }
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            subscriptionViewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        Text(userName ?: "—", style = MaterialTheme.typography.headlineSmall)
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(
                "Alumno",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        userEmail?.let { email ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        HorizontalDivider()

        // Stat único — reservas activas
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$activeBookingsCount", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Reservas activas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Suscripción
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Suscripción", style = MaterialTheme.typography.titleMedium)

                when (subState) {
                    is SubscriptionUiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))

                    is SubscriptionUiState.NoSubscription -> {
                        Text("Sin suscripción activa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Accede a todas las clases en vivo por 9,99€/mes", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { subscriptionViewModel.initiatePayment() },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            else Text("Suscribirse — 9,99€/mes")
                        }
                    }

                    is SubscriptionUiState.Active -> {
                        val sub = (subState as SubscriptionUiState.Active).subscription
                        val endDate = sub.ends_at?.let { formatEndDate(it) } ?: "—"
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.tertiaryContainer) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Text("Activa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                            }
                        }
                        Text("Válida hasta: $endDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar suscripción")
                        }

                        if (showCancelDialog) {
                            AlertDialog(
                                onDismissRequest = { showCancelDialog = false },
                                title = { Text("Cancelar suscripción") },
                                text = {
                                    Text(
                                        "¿Estás seguro de que quieres cancelar?\n\n" +
                                        "Tu suscripción seguirá activa hasta el $endDate. " +
                                        "A partir de esa fecha no se realizarán más cobros y perderás el acceso a las clases."
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showCancelDialog = false
                                            subscriptionViewModel.cancelSubscription()
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Sí, cancelar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showCancelDialog = false }) { Text("Volver") }
                                }
                            )
                        }
                    }

                    is SubscriptionUiState.Error -> Text((subState as SubscriptionUiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { profileViewModel.logout(onLogout) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Cerrar sesión") }
    }
}
