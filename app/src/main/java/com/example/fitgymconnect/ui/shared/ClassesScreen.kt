package com.example.fitgymconnect.ui.shared

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.fitgymconnect.ui.student.BookingUiState
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
    classViewModel: ClassViewModel = hiltViewModel(),
    detailViewModel: ClassDetailViewModel = hiltViewModel()
) {
    val classState    by classViewModel.state.collectAsState()
    val isRefreshing  by classViewModel.isRefreshing.collectAsState()
    val message       by bookingViewModel.message.collectAsState()
    val context       = LocalContext.current
    var selectedClass by remember { mutableStateOf<GymClass?>(null) }
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            bookingViewModel.clearMessage()
            if (it == "¡Reserva realizada!") {
                selectedClass = null
                detailViewModel.reset()
            }
        }
    }

    // Reset detail state when a new class is selected
    LaunchedEffect(selectedClass) { if (selectedClass == null) detailViewModel.reset() }

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
                            gymClass = gymClass,
                            onClick = if (showBookingButton) ({ selectedClass = gymClass }) else null
                        )
                    }
                }
            }
            } // PullToRefreshBox
        }
    }

    // Bottom sheet de reserva
    if (selectedClass != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedClass = null },
            sheetState = sheetState
        ) {
            BookingBottomSheet(
                gymClass = selectedClass!!,
                hasActiveSubscription = hasActiveSubscription,
                bookingViewModel = bookingViewModel,
                detailViewModel = detailViewModel
            )
        }
    }
}

@Composable
fun ClassCard(gymClass: GymClass, onClick: (() -> Unit)? = null) {
    val isOnline = gymClass.type == "online"
    val cardColor = if (isOnline) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Título
            Text(gymClass.title, style = MaterialTheme.typography.titleMedium)

            // Descripción
            gymClass.description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Entrenador
            gymClass.trainer?.user?.name?.let { name ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Hint de tap
            if (onClick != null) {
                Text(
                    "Pulsa para reservar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ScheduleSummaryRow(schedules: List<ClassSchedule>) {
    val grouped = schedules.groupBy { it.dayOfWeek }.toSortedMap()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        grouped.forEach { (dow, slots) ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                Text(dayOfWeekFull(dow), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(70.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    slots.forEach { schedule ->
                        Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(formatTimeSlot(schedule.timeSlot), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// ─── Bottom Sheet de reserva ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingBottomSheet(
    gymClass: GymClass,
    hasActiveSubscription: Boolean,
    bookingViewModel: BookingViewModel,
    detailViewModel: ClassDetailViewModel
) {
    val selectedDate     by detailViewModel.selectedDate.collectAsState()
    val selectedTimeSlot by detailViewModel.selectedTimeSlot.collectAsState()
    val availability     by detailViewModel.availability.collectAsState()
    val isLoadingAvail   by detailViewModel.isLoadingAvailability.collectAsState()
    val processingIds    by bookingViewModel.processingIds.collectAsState()
    val isProcessing     = gymClass.id in processingIds

    val schedules = gymClass.schedules ?: emptyList()
    val availableDaysOfWeek = schedules.map { it.dayOfWeek }.distinct()
    val allTimeSlots = remember(schedules) { schedules.map { it.timeSlot }.distinct().sorted() }

    val today = remember { LocalDate.now() }
    val nowTime = remember { java.time.LocalTime.now() }

    // Próximas fechas disponibles (4 semanas)
    val upcomingDates = remember(availableDaysOfWeek) {
        (0..27).map { today.plusDays(it.toLong()) }
            .filter { (it.dayOfWeek.value - 1) in availableDaysOfWeek }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Cabecera
        Text(gymClass.title, style = MaterialTheme.typography.titleLarge)

        // Sección: selección de fecha
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Selecciona una fecha", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(upcomingDates) { date ->
                    val isSelected = date == selectedDate
                    val slotsForDay = schedules.filter { it.dayOfWeek == date.dayOfWeek.value - 1 }.map { it.timeSlot }
                    val allSlotsPast = date == today && slotsForDay.all {
                        java.time.LocalTime.parse(formatTimeSlot(it)) <= nowTime
                    }
                    FilterChip(
                        selected = isSelected,
                        enabled = !allSlotsPast,
                        onClick = { detailViewModel.selectDate(gymClass.id, date) },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(dayOfWeekLabel(date.dayOfWeek.value - 1), style = MaterialTheme.typography.labelSmall)
                                Text(date.format(dateFormatter), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    )
                }
            }
        }

        // Sección: selección de horario (siempre visible)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Selecciona un horario", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allTimeSlots.forEach { slot ->
                    val isPast = selectedDate == today &&
                        java.time.LocalTime.parse(formatTimeSlot(slot)) <= nowTime
                    FilterChip(
                        selected = slot == selectedTimeSlot,
                        enabled = !isPast,
                        onClick = { detailViewModel.selectTimeSlot(gymClass.id, slot) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(formatTimeSlot(slot))
                            }
                        }
                    )
                }
            }
        }

        // Disponibilidad (cuando ambos están seleccionados)
        if (selectedDate != null && selectedTimeSlot != null) {
            when {
                isLoadingAvail -> CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                availability != null -> {
                    val avail = availability!!
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = if (avail.isFull) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            if (avail.isFull) "Clase completa — sin plazas disponibles"
                            else "${avail.availableSpots} de ${avail.maxStudents} plazas disponibles",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (avail.isFull) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Botón de reserva
        if (selectedDate != null && selectedTimeSlot != null) {
            when {
                !hasActiveSubscription -> Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Necesitas suscripción activa para reservar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                availability?.isFull == true -> Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Sin plazas disponibles") }
                else -> Button(
                    onClick = {
                        bookingViewModel.bookClass(
                            gymClass.id,
                            selectedDate!!.toString(),
                            formatTimeSlot(selectedTimeSlot!!)
                        )
                    },
                    enabled = !isProcessing && availability != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Confirmar reserva")
                }
            }
        }
    }
}
