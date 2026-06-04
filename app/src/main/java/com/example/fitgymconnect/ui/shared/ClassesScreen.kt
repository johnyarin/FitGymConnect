package com.example.fitgymconnect.ui.shared

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.Booking
import com.example.fitgymconnect.data.model.GymClass
import com.example.fitgymconnect.ui.student.BookingUiState
import com.example.fitgymconnect.ui.student.BookingViewModel
import com.example.fitgymconnect.utils.formatScheduledAt

@Composable
fun ClassesScreen(
    showBookingButton: Boolean = false,
    hasActiveSubscription: Boolean = true,
    filterByUserId: Int? = null,
    title: String = "Clases en vivo",
    classViewModel: ClassViewModel = hiltViewModel(),
    bookingViewModel: BookingViewModel = hiltViewModel()
) {
    val classState    by classViewModel.state.collectAsState()
    val bookingState  by bookingViewModel.state.collectAsState()
    val processingIds by bookingViewModel.processingIds.collectAsState()
    val message       by bookingViewModel.message.collectAsState()
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            bookingViewModel.clearMessage()
        }
    }

    val bookedMap: Map<Int, Booking> = when (bookingState) {
        is BookingUiState.Success -> (bookingState as BookingUiState.Success).bookings
            .filter { it.status != "cancelled" }
            .associateBy { it.class_id }
        else -> emptyMap()
    }

    when (classState) {
        is ClassUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is ClassUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text((classState as ClassUiState.Error).message)
                Button(onClick = { classViewModel.load() }) { Text("Reintentar") }
            }
        }
        is ClassUiState.Success -> {
            var classes = (classState as ClassUiState.Success).classes
            if (filterByUserId != null) classes = classes.filter { it.trainer?.user_id == filterByUserId }

            val types = classes.mapNotNull { it.type }.distinct()
            val filtered = if (selectedType == null) classes else classes.filter { it.type == selectedType }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 4.dp))
                }

                if (types.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedType == null,
                                    onClick = { selectedType = null },
                                    label = { Text("Todas") }
                                )
                            }
                            items(types, key = { it }) { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = if (selectedType == type) null else type },
                                    label = { Text(if (type == "online") "Online" else "Presencial") }
                                )
                            }
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.VideoCall,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                            Text(
                                when (selectedType) {
                                    "online"     -> "No hay clases online disponibles"
                                    "presencial" -> "No hay clases presenciales disponibles"
                                    else         -> "Aún no hay clases disponibles"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selectedType == null) {
                                Text(
                                    "Los entrenadores irán publicando clases pronto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { gymClass ->
                        ClassCard(
                            gymClass = gymClass,
                            showBookingButton = showBookingButton,
                            hasActiveSubscription = hasActiveSubscription,
                            booking = bookedMap[gymClass.id],
                            isProcessing = gymClass.id in processingIds,
                            onBook   = { bookingViewModel.bookClass(gymClass.id) },
                            onCancel = { bookedMap[gymClass.id]?.let { bookingViewModel.cancelBooking(it.id, gymClass.id) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClassCard(
    gymClass: GymClass,
    showBookingButton: Boolean = false,
    hasActiveSubscription: Boolean = true,
    booking: Booking? = null,
    isProcessing: Boolean = false,
    onBook: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val cardContainerColor = when (gymClass.type) {
        "online"     -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        "presencial" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else         -> MaterialTheme.colorScheme.surface
    }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(gymClass.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                gymClass.type?.let { type ->
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

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    gymClass.scheduled_at?.let { formatScheduledAt(it) } ?: "Fecha por confirmar",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (gymClass.scheduled_at != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            gymClass.trainer?.user?.name?.let { name ->
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            gymClass.max_capacity?.let { cap ->
                val free = cap - (gymClass.bookings_count ?: 0)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.EventSeat, contentDescription = null, modifier = Modifier.size(14.dp),
                        tint = if (free == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    Text(
                        if (free == 0) "Sin plazas disponibles" else "$free de $cap plazas libres",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (free == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showBookingButton) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                when {
                    !hasActiveSubscription -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Necesitas suscripción activa para reservar clases", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    booking != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("Plaza reservada — cancela desde la pestaña Reservas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    else -> {
                        Button(onClick = onBook, enabled = !isProcessing, modifier = Modifier.fillMaxWidth()) {
                            if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            else Text("Reservar plaza")
                        }
                    }
                }
            }
        }
    }
}
