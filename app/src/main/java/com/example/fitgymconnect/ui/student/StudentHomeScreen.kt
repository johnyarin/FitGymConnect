package com.example.fitgymconnect.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.Booking
import com.example.fitgymconnect.data.model.Routine
import com.example.fitgymconnect.ui.shared.ProfileViewModel
import com.example.fitgymconnect.ui.shared.RoutineUiState
import com.example.fitgymconnect.ui.shared.RoutineViewModel
import com.example.fitgymconnect.utils.difficultyLabel
import com.example.fitgymconnect.utils.formatBookingDateTime
import com.example.fitgymconnect.utils.formatTimeSlot
import java.time.LocalDate

@Composable
fun StudentHomeScreen(
    bookingViewModel: BookingViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    onNavigateToClases: () -> Unit = {},
    onNavigateToRutinas: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val userName     by profileViewModel.userName.collectAsState(initial = null)
    val subState     by subscriptionViewModel.state.collectAsState()
    val bookingState by bookingViewModel.state.collectAsState()
    val routineState by routineViewModel.state.collectAsState()
    val hasActiveSub = subState is SubscriptionUiState.Active

    val today = LocalDate.now().toString()

    val upcomingBookings: List<Booking> = when (bookingState) {
        is BookingUiState.Success -> (bookingState as BookingUiState.Success).bookings
            .filter { it.status != "cancelled" && it.booking_date != null && it.booking_date >= today }
            .sortedWith(compareBy({ it.booking_date }, { it.time_slot }))
            .take(2)
        else -> emptyList()
    }

    val previewRoutines: List<Routine> = when (routineState) {
        is RoutineUiState.Success -> {
            val all = (routineState as RoutineUiState.Success).routines
            if (hasActiveSub) all.take(3) else all.filter { it.is_premium != true }.take(3)
        }
        else -> emptyList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "¡Bienvenido${if (userName != null) ", ${userName!!.substringBefore(" ")}" else ""}!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text("¿Listo para entrenar hoy?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (subState is SubscriptionUiState.NoSubscription) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Hazte Premium", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Accede a todas las clases en vivo y rutinas premium por 9,99€/mes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Button(onClick = onNavigateToPerfil, modifier = Modifier.align(Alignment.End)) { Text("Ver planes") }
                    }
                }
            }
        }

        item {
            HomeSectionHeader(title = "Próximas clases")
            Spacer(Modifier.height(8.dp))
            if (upcomingBookings.isEmpty()) {
                HomeEmptyCard(text = "No tienes reservas próximas")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    upcomingBookings.forEach { NextBookingCard(it) }
                }
            }
        }

        item {
            HomeSectionHeader(title = "Rutinas para ti")
            Spacer(Modifier.height(8.dp))
            if (previewRoutines.isEmpty()) {
                HomeEmptyCard(text = "No hay rutinas disponibles")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    previewRoutines.forEach { CompactRoutineCard(it) }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (actionLabel != null) {
            TextButton(onClick = onAction) { Text(actionLabel, style = MaterialTheme.typography.labelMedium) }
        }
    }
}

@Composable
private fun HomeEmptyCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NextBookingCard(booking: Booking) {
    val gymClass = booking.gym_class
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(gymClass?.title ?: "Clase reservada", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            if (booking.booking_date != null && booking.time_slot != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(formatBookingDateTime(booking.booking_date, booking.time_slot), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            gymClass?.trainer?.user?.name?.let { name ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
private fun CompactRoutineCard(routine: Routine) {
    val cardColor = when (routine.difficulty) {
        "beginner"     -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        "intermediate" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        "advanced"     -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else           -> MaterialTheme.colorScheme.surface
    }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(routine.title, style = MaterialTheme.typography.bodyMedium)
                routine.trainer?.user?.name?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                routine.difficulty?.let { diff ->
                    Surface(shape = MaterialTheme.shapes.small, color = when (diff) {
                        "beginner" -> MaterialTheme.colorScheme.tertiaryContainer
                        "intermediate" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }) {
                        Text(difficultyLabel(diff), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
                routine.duration?.let { dur ->
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("$dur min", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}
