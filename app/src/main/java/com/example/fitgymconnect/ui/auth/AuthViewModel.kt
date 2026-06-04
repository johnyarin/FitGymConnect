package com.example.fitgymconnect.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.repository.AuthRepository
import com.example.fitgymconnect.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        _uiState.value = when (val r = repo.login(email, password)) {
            is Result.Success -> AuthUiState.Success(r.data.role)
            is Result.Error   -> AuthUiState.Error(r.message)
        }
    }

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        _uiState.value = when (val r = repo.register(name, email, password)) {
            is Result.Success -> AuthUiState.Success(r.data.role)
            is Result.Error   -> AuthUiState.Error(r.message)
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}
