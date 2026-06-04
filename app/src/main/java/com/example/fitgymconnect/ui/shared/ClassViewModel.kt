package com.example.fitgymconnect.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.model.GymClass
import com.example.fitgymconnect.data.repository.ClassRepository
import com.example.fitgymconnect.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClassUiState {
    object Loading : ClassUiState()
    data class Success(val classes: List<GymClass>) : ClassUiState()
    data class Error(val message: String) : ClassUiState()
}

@HiltViewModel
class ClassViewModel @Inject constructor(
    private val repo: ClassRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ClassUiState>(ClassUiState.Loading)
    val state: StateFlow<ClassUiState> = _state

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ClassUiState.Loading
        _state.value = when (val r = repo.getClasses()) {
            is Result.Success -> ClassUiState.Success(r.data)
            is Result.Error   -> ClassUiState.Error(r.message)
        }
    }
}
