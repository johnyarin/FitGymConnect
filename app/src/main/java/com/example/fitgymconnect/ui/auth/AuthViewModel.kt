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
    data class PendingVerification(val email: String) : AuthUiState()
}

sealed class ResendState {
    object Idle : ResendState()
    object Loading : ResendState()
    data class Done(val success: Boolean, val message: String) : ResendState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _resendState = MutableStateFlow<ResendState>(ResendState.Idle)
    val resendState: StateFlow<ResendState> = _resendState

    fun login(email: String, password: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        _uiState.value = when (val r = repo.login(email, password)) {
            is Result.Success -> {
                if (r.data.emailVerified) AuthUiState.Success(r.data.role)
                else AuthUiState.PendingVerification(r.data.userEmail)
            }
            is Result.Error -> AuthUiState.Error(r.message)
        }
    }

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        _uiState.value = when (val r = repo.register(name, email, password)) {
            is Result.Success -> {
                if (r.data.emailVerified) AuthUiState.Success(r.data.role)
                else AuthUiState.PendingVerification(r.data.userEmail)
            }
            is Result.Error -> AuthUiState.Error(r.message)
        }
    }

    fun resendVerification() = viewModelScope.launch {
        _resendState.value = ResendState.Loading
        _resendState.value = when (repo.resendVerification()) {
            is Result.Success -> ResendState.Done(true, "Email reenviado. Revisa tu bandeja de entrada.")
            is Result.Error   -> ResendState.Done(false, "No se pudo reenviar. Inténtalo de nuevo.")
        }
    }

    fun checkVerification() = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        _uiState.value = when (val r = repo.checkVerification()) {
            is Result.Success -> {
                if (r.data.email_verified_at != null) AuthUiState.Success(r.data.role)
                else AuthUiState.Error("Aún no hemos recibido tu verificación. Revisa tu email.")
            }
            is Result.Error -> AuthUiState.Error(r.message)
        }
    }

    fun resetResendState() { _resendState.value = ResendState.Idle }
    fun resetState() { _uiState.value = AuthUiState.Idle }
}
