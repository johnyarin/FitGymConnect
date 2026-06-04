package com.example.fitgymconnect.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = RoutineUiState.Loading
        _state.value = when (val r = repo.getRoutines()) {
            is Result.Success -> RoutineUiState.Success(r.data)
            is Result.Error   -> RoutineUiState.Error(r.message)
        }
    }
}
