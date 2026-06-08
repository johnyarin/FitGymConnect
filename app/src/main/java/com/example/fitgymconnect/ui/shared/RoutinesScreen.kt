package com.example.fitgymconnect.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitgymconnect.data.model.Routine
import com.example.fitgymconnect.utils.difficultyLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    filterByUserId: Int? = null,
    title: String = "Rutinas",
    hasActiveSubscription: Boolean = true,
    onRoutineClick: ((Routine) -> Unit)? = null,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }

    when (state) {
        is RoutineUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is RoutineUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text((state as RoutineUiState.Error).message)
                Button(onClick = { viewModel.load() }) { Text("Reintentar") }
            }
        }
        is RoutineUiState.Success -> {
            var routines = (state as RoutineUiState.Success).routines
            if (filterByUserId != null) {
                routines = routines.filter { it.trainer?.user_id == filterByUserId }
            }

            val difficulties = routines.mapNotNull { it.difficulty }.distinct()
            val filtered = if (selectedDifficulty == null) routines
                           else routines.filter { it.difficulty == selectedDifficulty }

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
                item {
                    Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 4.dp))
                }

                if (difficulties.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedDifficulty == null,
                                    onClick = { selectedDifficulty = null },
                                    label = { Text("Todos") }
                                )
                            }
                            items(difficulties, key = { it }) { diff ->
                                FilterChip(
                                    selected = selectedDifficulty == diff,
                                    onClick = { selectedDifficulty = if (selectedDifficulty == diff) null else diff },
                                    label = { Text(difficultyLabel(diff)) }
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
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                            Text(
                                if (selectedDifficulty != null) "No hay rutinas de nivel ${difficultyLabel(selectedDifficulty!!)}"
                                else "Aún no hay rutinas disponibles",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selectedDifficulty == null) {
                                Text(
                                    "Los entrenadores irán añadiendo rutinas pronto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) {
                        RoutineCard(it, hasActiveSubscription, onClick = if (onRoutineClick != null && it.is_premium != true || hasActiveSubscription) ({ onRoutineClick?.invoke(it) }) else null)
                    }
                }
            }
            } // PullToRefreshBox
        }
    }
}

@Composable
fun RoutineCard(routine: Routine, hasActiveSubscription: Boolean = true, onClick: (() -> Unit)? = null) {
    val locked = routine.is_premium == true && !hasActiveSubscription

    val cardContainerColor = when {
        locked -> MaterialTheme.colorScheme.surfaceVariant
        else -> when (routine.difficulty) {
            "beginner"     -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            "intermediate" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            "advanced"     -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else           -> MaterialTheme.colorScheme.surface
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    color = if (locked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (routine.is_premium == true) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text(
                            "Premium",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            if (locked) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Requiere suscripción activa para acceder a esta rutina", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    routine.difficulty?.let { diff ->
                        val (diffColor, diffOnColor) = when (diff) {
                            "beginner"     -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                            "intermediate" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                            "advanced"     -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                            else           -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(shape = MaterialTheme.shapes.small, color = diffColor) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(10.dp), tint = diffOnColor)
                                Text(difficultyLabel(diff), style = MaterialTheme.typography.labelSmall, color = diffOnColor)
                            }
                        }
                    }
                    routine.duration?.let { dur ->
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("$dur min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                routine.description?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                routine.trainer?.user?.name?.let { name ->
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
