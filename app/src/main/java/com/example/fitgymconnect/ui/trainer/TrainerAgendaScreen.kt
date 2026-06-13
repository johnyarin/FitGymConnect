package com.example.fitgymconnect.ui.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.ClassBookingItem
import com.example.fitgymconnect.utils.dayOfWeekFull
import com.example.fitgymconnect.utils.formatTimeSlot
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerAgendaScreen(
    agendaViewModel: TrainerAgendaViewModel = hiltViewModel()
) {
    val agendaState  by agendaViewModel.state.collectAsState()
    var selectedItem by remember { mutableStateOf<AgendaItem?>(null) }
    val sheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when (agendaState) {
        is TrainerAgendaViewModel.AgendaState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is TrainerAgendaViewModel.AgendaState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text((agendaState as TrainerAgendaViewModel.AgendaState.Error).message)
        }
        is TrainerAgendaViewModel.AgendaState.Success -> {
            val items = (agendaState as TrainerAgendaViewModel.AgendaState.Success).items

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Mis clases", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp))
                }

                if (items.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                            Text("No tienes clases en los próximos 14 días", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    // Cabecera de fecha cuando cambia
                    var lastDate: LocalDate? = null
                    items.forEach { item ->
                        if (item.date != lastDate) {
                            lastDate = item.date
                            item {
                                Text(
                                    formatAgendaDate(item.date),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = if (items.first() == item) 0.dp else 8.dp, bottom = 4.dp)
                                )
                            }
                        }
                        item(key = "${item.gymClass.id}_${item.date}_${item.schedule.timeSlot}") {
                            AgendaCard(item = item, onClick = { selectedItem = item })
                        }
                    }
                }
            }
        }
    }

    if (selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedItem = null },
            sheetState = sheetState
        ) {
            StudentListBottomSheet(item = selectedItem!!)
        }
    }
}

@Composable
private fun AgendaCard(item: AgendaItem, onClick: () -> Unit) {
    val booked = item.bookings.size
    val max    = item.schedule.maxStudents
    val isFull = booked >= max

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Text(formatTimeSlot(item.schedule.timeSlot), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            VerticalDivider(modifier = Modifier.height(36.dp))

            Text(item.gymClass.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))

            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isFull) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp),
                        tint = if (isFull) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("$booked/$max",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFull) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
private fun StudentListBottomSheet(item: AgendaItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.gymClass.title, style = MaterialTheme.typography.titleLarge)
            Text(
                "${formatAgendaDate(item.date)} • ${formatTimeSlot(item.schedule.timeSlot)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        if (item.bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Text("Sin reservas para esta sesión", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text("${item.bookings.size} alumno${if (item.bookings.size != 1) "s" else ""}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.bookings.forEach { booking ->
                    StudentRow(booking)
                }
            }
        }
    }
}

@Composable
private fun StudentRow(booking: ClassBookingItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    booking.user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text(booking.user?.name ?: "Alumno", style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatAgendaDate(date: LocalDate): String {
    val today    = LocalDate.now()
    val tomorrow = today.plusDays(1)
    return when (date) {
        today    -> "Hoy • ${dayOfWeekFull(date.dayOfWeek.value - 1)} ${date.dayOfMonth} ${monthLabel(date.monthValue)}"
        tomorrow -> "Mañana • ${dayOfWeekFull(date.dayOfWeek.value - 1)} ${date.dayOfMonth} ${monthLabel(date.monthValue)}"
        else     -> "${dayOfWeekFull(date.dayOfWeek.value - 1)} ${date.dayOfMonth} ${monthLabel(date.monthValue)}"
    }
}

private fun monthLabel(month: Int) = when (month) {
    1 -> "Ene"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Abr"
    5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Ago"
    9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dic"
    else -> ""
}
