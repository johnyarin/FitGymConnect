package com.example.fitgymconnect.ui.student

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.Booking
import com.example.fitgymconnect.utils.formatScheduledAt
import com.example.fitgymconnect.utils.statusLabel

@Composable
fun MyBookingsScreen(viewModel: BookingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val processingIds by viewModel.processingIds.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    when (state) {
        is BookingUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is BookingUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text((state as BookingUiState.Error).message)
                Button(onClick = { viewModel.loadBookings() }) { Text("Reintentar") }
            }
        }
        is BookingUiState.Success -> {
            val bookings = (state as BookingUiState.Success).bookings.filter { it.status != "cancelled" }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("Mis Reservas", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 4.dp)) }
                if (bookings.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                            Text(
                                "No tienes reservas activas",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Explora las clases disponibles y reserva tu plaza",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    items(bookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            isProcessing = booking.class_id in processingIds,
                            onCancel = { viewModel.cancelBooking(booking.id, booking.class_id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking, isProcessing: Boolean, onCancel: () -> Unit) {
    val gymClass = booking.gym_class
    val uriHandler = LocalUriHandler.current
    var showCancelDialog by remember { mutableStateOf(false) }

    val cardContainerColor = when (gymClass?.type) {
        "online"     -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        "presencial" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else         -> MaterialTheme.colorScheme.surface
    }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Título + badge tipo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    gymClass?.title ?: "Clase #${booking.class_id}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                gymClass?.type?.let { type ->
                    val isOnline = type == "online"
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            if (isOnline) "Online" else "Presencial",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Fecha
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    gymClass?.scheduled_at?.let { formatScheduledAt(it) } ?: "Fecha por confirmar",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (gymClass?.scheduled_at != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Entrenador
            gymClass?.trainer?.user?.name?.let { name ->
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Estado
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    statusLabel(booking.status),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Enlace videollamada para clases online
            if (gymClass?.type == "online") {
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (gymClass.meet_link != null) Modifier.clickable { uriHandler.openUri(gymClass.meet_link) } else Modifier),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enlace de la clase", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            if (gymClass.meet_link != null) {
                                Text(gymClass.meet_link, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            } else {
                                Text("Disponible antes de la clase", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        if (gymClass.meet_link != null) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showCancelDialog = true },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Cancelar reserva")
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Estás seguro de que quieres cancelar tu plaza en \"${gymClass?.title ?: "esta clase"}\"?") },
            confirmButton = {
                TextButton(
                    onClick = { showCancelDialog = false; onCancel() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Volver") }
            }
        )
    }
}
