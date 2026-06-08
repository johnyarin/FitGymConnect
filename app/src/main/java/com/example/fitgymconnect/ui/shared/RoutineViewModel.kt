package com.example.fitgymconnect.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.model.Exercise
import com.example.fitgymconnect.data.model.Routine
import com.example.fitgymconnect.data.repository.Result
import com.example.fitgymconnect.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RoutineUiState {
    object Loading : RoutineUiState()
    data class Success(val routines: List<Routine>) : RoutineUiState()
    data class Error(val message: String) : RoutineUiState()
}

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repo: RoutineRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RoutineUiState>(RoutineUiState.Loading)
    val state: StateFlow<RoutineUiState> = _state

    private val _selectedRoutine = MutableStateFlow<Routine?>(null)
    val selectedRoutine: StateFlow<Routine?> = _selectedRoutine

    private val _selectedExercise = MutableStateFlow<Exercise?>(null)
    val selectedExercise: StateFlow<Exercise?> = _selectedExercise

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = RoutineUiState.Loading
        _state.value = when (val r = repo.getRoutines()) {
            is Result.Success -> RoutineUiState.Success(r.data)
            is Result.Error   -> RoutineUiState.Error(r.message)
        }
    }

    fun refresh() = viewModelScope.launch {
        _isRefreshing.value = true
        when (val r = repo.getRoutines()) {
            is Result.Success -> _state.value = RoutineUiState.Success(r.data)
            is Result.Error   -> Unit
        }
        _isRefreshing.value = false
    }

    fun selectRoutine(routine: Routine) { _selectedRoutine.value = routine }
    fun selectExercise(exercise: Exercise) { _selectedExercise.value = exercise }
}
