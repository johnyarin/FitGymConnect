package com.example.fitgymconnect.ui.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.Routine
import com.example.fitgymconnect.ui.shared.ProfileViewModel
import com.example.fitgymconnect.ui.shared.RoutineUiState
import com.example.fitgymconnect.ui.shared.RoutineViewModel
import com.example.fitgymconnect.utils.difficultyLabel
import com.example.fitgymconnect.utils.formatTimeSlot
import java.time.LocalDate

@Composable
fun TrainerHomeScreen(
    agendaViewModel: TrainerAgendaViewModel,
    onNavigateToClases: () -> Unit = {},
    onNavigateToRutinas: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val userName     by profileViewModel.userName.collectAsState(initial = null)
    val userId       by profileViewModel.userId.collectAsState(initial = null)
    val agendaState  by agendaViewModel.state.collectAsState()
    val routineState by routineViewModel.state.collectAsState()

    val today = LocalDate.now()

    // Sesiones de hoy; si no hay, las 2 próximas
    val upcomingSessions: List<AgendaItem> = when (agendaState) {
        is TrainerAgendaViewModel.AgendaState.Success -> {
            val items = (agendaState as TrainerAgendaViewModel.AgendaState.Success).items
            val todaySessions = items.filter { it.date == today }
            if (todaySessions.isNotEmpty()) todaySessions else items.take(2)
        }
        else -> emptyList()
    }

    val myRoutines: List<Routine> = when (routineState) {
        is RoutineUiState.Success -> (routineState as RoutineUiState.Success).routines
            .filter { it.trainer?.user_id == userId }.take(3)
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
                "¡Hola${if (userName != null) ", ${userName!!.substringBefore(" ")}" else ""}!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Aquí tienes un resumen de tu jornada.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            HomeSectionHeader(
                title = if (upcomingSessions.any { it.date == today }) "Hoy" else "Próximas sesiones",
                actionLabel = "Ver agenda",
                onAction = onNavigateToClases
            )
            Spacer(Modifier.height(8.dp))

            if (agendaState is TrainerAgendaViewModel.AgendaState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else if (upcomingSessions.isEmpty()) {
                TrainerEmptyCard("No tienes sesiones programadas próximamente")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    upcomingSessions.forEach { session ->
                        HomeSessionCard(session)
                    }
                }
            }
        }

        item {
            HomeSectionHeader(title = "Mis rutinas", actionLabel = "Ver todas", onAction = onNavigateToRutinas)
            Spacer(Modifier.height(8.dp))
            if (myRoutines.isEmpty()) {
                TrainerEmptyCard("No tienes rutinas publicadas")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    myRoutines.forEach { CompactRoutineCard(it) }
                }
            }
        }
    }
}

@Composable
private fun HomeSessionCard(session: AgendaItem) {
    val booked = session.bookings.size
    val max    = session.schedule.maxStudents
    val isFull = booked >= max

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(formatTimeSlot(session.schedule.timeSlot), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            VerticalDivider(modifier = Modifier.height(36.dp))
            Text(session.gymClass.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSecondaryContainer)
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isFull) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp),
                        tint = if (isFull) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("$booked/$max", style = MaterialTheme.typography.labelSmall,
                        color = if (isFull) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) { Text(actionLabel, style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
private fun TrainerEmptyCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                if (routine.is_premium == true) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text("Premium", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
            routine.difficulty?.let { diff ->
                Surface(shape = MaterialTheme.shapes.small, color = when (diff) {
                    "beginner" -> MaterialTheme.colorScheme.tertiaryContainer
                    "intermediate" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }) {
                    Text(difficultyLabel(diff), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
