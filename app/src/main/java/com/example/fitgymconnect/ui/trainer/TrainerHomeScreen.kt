package com.example.fitgymconnect.ui.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.GymClass
import com.example.fitgymconnect.data.model.Routine
import com.example.fitgymconnect.ui.shared.ClassUiState
import com.example.fitgymconnect.ui.shared.ClassViewModel
import com.example.fitgymconnect.ui.shared.ProfileViewModel
import com.example.fitgymconnect.ui.shared.RoutineUiState
import com.example.fitgymconnect.ui.shared.RoutineViewModel
import com.example.fitgymconnect.utils.difficultyLabel
import com.example.fitgymconnect.utils.formatScheduledAt

@Composable
fun TrainerHomeScreen(
    onNavigateToClases: () -> Unit = {},
    onNavigateToRutinas: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    classViewModel: ClassViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val userName by profileViewModel.userName.collectAsState(initial = null)
    val userId by profileViewModel.userId.collectAsState(initial = null)
    val classState by classViewModel.state.collectAsState()
    val routineState by routineViewModel.state.collectAsState()

    val myClasses: List<GymClass> = when (classState) {
        is ClassUiState.Success -> (classState as ClassUiState.Success).classes
            .filter { it.trainer?.user_id == userId }
        else -> emptyList()
    }

    val myRoutines: List<Routine> = when (routineState) {
        is RoutineUiState.Success -> (routineState as RoutineUiState.Success).routines
            .filter { it.trainer?.user_id == userId }
        else -> emptyList()
    }

    val nextClass = myClasses.firstOrNull()
    val previewRoutines = myRoutines.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Saludo
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "¡Hola${if (userName != null) ", ${userName!!.substringBefore(" ")}" else ""}!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Aquí tienes un resumen de tu actividad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stats rápidos
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${myClasses.size}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Clases", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${myRoutines.size}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Rutinas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Próxima clase
        item {
            TrainerSectionHeader(title = "Próxima clase", actionLabel = "Ver todas", onAction = onNavigateToClases)
            Spacer(Modifier.height(8.dp))
            if (nextClass == null) {
                TrainerEmptyCard(text = "No tienes clases programadas", actionLabel = "Ver clases", onAction = onNavigateToClases)
            } else {
                NextClassCard(gymClass = nextClass)
            }
        }

        // Mis rutinas
        item {
            TrainerSectionHeader(title = "Mis rutinas", actionLabel = "Ver todas", onAction = onNavigateToRutinas)
            Spacer(Modifier.height(8.dp))
            if (previewRoutines.isEmpty()) {
                TrainerEmptyCard(text = "No tienes rutinas publicadas", actionLabel = "Ver rutinas", onAction = onNavigateToRutinas)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    previewRoutines.forEach { TrainerCompactRoutineCard(it) }
                }
            }
        }
    }
}

@Composable
private fun TrainerSectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) {
            Text(actionLabel, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TrainerEmptyCard(text: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun NextClassCard(gymClass: GymClass) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    gymClass.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                gymClass.type?.let { type ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (type == "online") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            if (type == "online") "Online" else "Presencial",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (type == "online") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                    gymClass.scheduled_at?.let { formatScheduledAt(it) } ?: "Fecha por confirmar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            gymClass.max_capacity?.let { cap ->
                val occupied = gymClass.bookings_count ?: 0
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventSeat, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        "$occupied de $cap plazas ocupadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainerCompactRoutineCard(routine: Routine) {
    val cardContainerColor = when (routine.difficulty) {
        "beginner"     -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        "intermediate" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        "advanced"     -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else           -> MaterialTheme.colorScheme.surface
    }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(routine.title, style = MaterialTheme.typography.bodyMedium)
                if (routine.is_premium == true) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text("Premium", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                routine.difficulty?.let { diff ->
                    val color = when (diff) {
                        "beginner"     -> MaterialTheme.colorScheme.tertiaryContainer
                        "intermediate" -> MaterialTheme.colorScheme.secondaryContainer
                        "advanced"     -> MaterialTheme.colorScheme.errorContainer
                        else           -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Surface(shape = MaterialTheme.shapes.small, color = color) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(9.dp))
                            Text(difficultyLabel(diff), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                routine.duration?.let { dur ->
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(9.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("$dur min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}
