package com.example.fitgymconnect.ui.student

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.Booking
import com.example.fitgymconnect.utils.formatBookingDateTime
import com.example.fitgymconnect.utils.formatTimeSlot
import com.example.fitgymconnect.utils.statusLabel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(viewModel: BookingViewModel = hiltViewModel()) {
    val state         by viewModel.state.collectAsState()
    val processingIds by viewModel.processingIds.collectAsState()
    val message       by viewModel.message.collectAsState()
    val isRefreshing  by viewModel.isRefreshing.collectAsState()
    val context      = LocalContext.current

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
            val today = LocalDate.now().toString()
            val bookings = (state as BookingUiState.Success).bookings.filter { booking ->
                booking.status != "cancelled" &&
                booking.booking_date != null &&
                booking.booking_date >= today
            }
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("Mis Reservas", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 4.dp)) }
                if (bookings.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                            Text("No tienes reservas activas", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Explora las clases disponibles y reserva tu plaza", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                } else {
                    items(bookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking      = booking,
                            isProcessing = booking.id in processingIds,
                            onCancel     = { viewModel.cancelBooking(booking.id) }
                        )
                    }
                }
            }
            } // PullToRefreshBox
        }
    }
}

@Composable
fun BookingCard(booking: Booking, isProcessing: Boolean, onCancel: () -> Unit) {
    val gymClass   = booking.gym_class
    val uriHandler = LocalUriHandler.current
    var showCancelDialog by remember { mutableStateOf(false) }

    val isOnline = gymClass?.type == "online"
    val cardColor = if (isOnline) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(gymClass?.title ?: "Clase reservada", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { showCancelDialog = true },
                    enabled = !isProcessing,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    else Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (booking.booking_date != null && booking.time_slot != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(formatBookingDateTime(booking.booking_date, booking.time_slot), style = MaterialTheme.typography.bodySmall)
                }
            }

            gymClass?.trainer?.user?.name?.let { name ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Enlace videollamada (solo online)
            if (isOnline && gymClass?.meet_link != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(gymClass.meet_link) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enlace de la clase", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(gymClass.meet_link, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Estás seguro de que quieres cancelar tu plaza en \"${gymClass?.title ?: "esta clase"}\"?") },
            confirmButton = {
                TextButton(onClick = { showCancelDialog = false; onCancel() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Volver") }
            }
        )
    }
}
