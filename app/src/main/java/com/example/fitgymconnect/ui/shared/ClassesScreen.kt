package com.example.fitgymconnect.ui.shared

import android.widget.Toast
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.ClassSchedule
import com.example.fitgymconnect.data.model.GymClass
import com.example.fitgymconnect.ui.student.BookingViewModel
import com.example.fitgymconnect.utils.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    filterByUserId: Int? = null,
    title: String = "Clases",
    showBookingButton: Boolean = false,
    hasActiveSubscription: Boolean = true,
    bookingViewModel: BookingViewModel = hiltViewModel(),
    classViewModel: ClassViewModel = hiltViewModel()
) {
    val classState    by classViewModel.state.collectAsState()
    val isRefreshing  by classViewModel.isRefreshing.collectAsState()
    val processingIds by bookingViewModel.processingIds.collectAsState()
    val message       by bookingViewModel.message.collectAsState()
    val context       = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            bookingViewModel.clearMessage()
        }
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

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { classViewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    if (classes.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                                Text("Aún no hay clases disponibles", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Los entrenadores irán publicando clases pronto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    } else {
                        items(classes, key = { it.id }) { gymClass ->
                            ClassCard(
                                gymClass              = gymClass,
                                showBookingButton     = showBookingButton,
                                hasActiveSubscription = hasActiveSubscription,
                                isProcessing          = gymClass.id in processingIds,
                                onBook                = { date, slot ->
                                    bookingViewModel.bookClass(gymClass.id, date, slot)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Devuelve la próxima fecha+horario disponible para la clase (mínimo mañana por validación del backend)
private fun nextBookingSlot(schedules: List<ClassSchedule>): Pair<LocalDate, String>? {
    if (schedules.isEmpty()) return null
    val start = LocalDate.now().plusDays(1)
    for (daysAhead in 0..13) {
        val date = start.plusDays(daysAhead.toLong())
        val dow  = date.dayOfWeek.value - 1  // 0=Lun..6=Dom
        val slot = schedules
            .filter { it.dayOfWeek == dow }
            .map { it.timeSlot }
            .minOrNull()
        if (slot != null) return Pair(date, slot)
    }
    return null
}

@Composable
fun ClassCard(
    gymClass: GymClass,
    showBookingButton: Boolean = false,
    hasActiveSubscription: Boolean = true,
    isProcessing: Boolean = false,
    onBook: ((date: String, timeSlot: String) -> Unit)? = null,
    availabilityViewModel: ClassDetailViewModel = hiltViewModel(key = "avail_${gymClass.id}")
) {
    val isOnline  = gymClass.type == "online"
    val cardColor = if (isOnline) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)

    val availability        by availabilityViewModel.availability.collectAsState()
    val isLoadingAvailability by availabilityViewModel.isLoadingAvailability.collectAsState()

    val nextSlot = remember(gymClass.schedules) {
        nextBookingSlot(gymClass.schedules ?: emptyList())
    }
    val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale("es", "ES"))

    LaunchedEffect(nextSlot) {
        if (nextSlot != null && showBookingButton) {
            availabilityViewModel.loadForSlot(gymClass.id, nextSlot.first, nextSlot.second)
        }
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors    = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Text(gymClass.title, style = MaterialTheme.typography.titleMedium)

            gymClass.description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            gymClass.trainer?.user?.name?.let { name ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (showBookingButton) {
                if (nextSlot == null) {
                    Text("Sin próximas fechas disponibles", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "${nextSlot.first.format(dateFormatter)}, ${formatTimeSlot(nextSlot.second)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Aforo
                    when {
                        isLoadingAvailability -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp)
                                Text("Comprobando disponibilidad...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        availability != null -> {
                            val isFull = availability!!.isFull
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    if (isFull) "Clase completa"
                                    else "${availability!!.availableSpots}/${availability!!.maxStudents} plazas disponibles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!hasActiveSubscription) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Necesitas suscripción activa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        val isFull = availability?.isFull == true
                        Button(
                            onClick  = { onBook?.invoke(nextSlot.first.toString(), formatTimeSlot(nextSlot.second)) },
                            enabled  = !isProcessing && !isFull && !isLoadingAvailability,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isProcessing)
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            else
                                Text(if (isFull) "Sin plazas" else "Reservar")
                        }
                    }
                }
            }
        }
    }
}
