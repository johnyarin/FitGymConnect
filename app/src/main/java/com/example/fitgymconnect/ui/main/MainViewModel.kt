package com.example.fitgymconnect.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.local.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val role: String) : AuthState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    tokenDataStore: TokenDataStore
) : ViewModel() {

    val authState = combine(tokenDataStore.token, tokenDataStore.role) { token, role ->
        when {
            token == null -> AuthState.Unauthenticated
            role != null  -> AuthState.Authenticated(role)
            else          -> AuthState.Unauthenticated
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Loading)
}
