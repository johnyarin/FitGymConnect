package com.example.fitgymconnect.ui.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.ui.shared.ProfileViewModel
import com.example.fitgymconnect.ui.theme.LocalIsDarkTheme
import com.example.fitgymconnect.ui.theme.LocalToggleDarkTheme
import com.example.fitgymconnect.ui.shared.RoutineUiState
import com.example.fitgymconnect.ui.shared.RoutineViewModel
import com.example.fitgymconnect.utils.formatTimeSlot
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TrainerProfileScreen(
    onLogout: () -> Unit,
    agendaViewModel: TrainerAgendaViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel  = hiltViewModel()
) {
    val isDarkTheme = LocalIsDarkTheme.current
    val toggleTheme = LocalToggleDarkTheme.current

    val userName   by profileViewModel.userName.collectAsState(initial = null)
    val userEmail  by profileViewModel.userEmail.collectAsState(initial = null)
    val userId     by profileViewModel.userId.collectAsState(initial = null)
    val agendaState by agendaViewModel.state.collectAsState()
    val routineState by routineViewModel.state.collectAsState()

    val today = LocalDate.now()
    val now   = LocalTime.now()

    val agendaItems = when (agendaState) {
        is TrainerAgendaViewModel.AgendaState.Success ->
            (agendaState as TrainerAgendaViewModel.AgendaState.Success).items
        else -> emptyList()
    }

    val remainingToday = agendaItems.filter { item ->
        item.date == today &&
        LocalTime.parse(formatTimeSlot(item.schedule.timeSlot)) > now
    }
    val remainingSessionsCount = remainingToday.size
    val studentsRemainingToday = remainingToday.sumOf { it.bookings.size }

    val routineCount = when (routineState) {
        is RoutineUiState.Success -> (routineState as RoutineUiState.Success).routines
            .count { it.trainer?.user_id == userId }
        else -> 0
    }

    // Especialidad obtenida desde los datos de agenda, no del perfil directamente
    val specialty = agendaItems.firstOrNull()?.gymClass?.trainer?.specialty

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    userName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(userName ?: "—", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                Text("Entrenador", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            specialty?.let {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }

        userEmail?.let { email ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        HorizontalDivider()

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatColumn(value = "$remainingSessionsCount", label = "Sesiones\nrestantes hoy")
                VerticalDivider(modifier = Modifier.height(40.dp))
                StatColumn(value = "$studentsRemainingToday", label = "Alumnos\npara hoy")
                VerticalDivider(modifier = Modifier.height(40.dp))
                StatColumn(value = "$routineCount", label = "Rutinas\npublicadas")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Modo oscuro", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { toggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor   = MaterialTheme.colorScheme.primary,
                        checkedThumbColor   = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
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

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
