package com.example.fitgymconnect.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.local.TokenDataStore
import com.example.fitgymconnect.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    val userName  = tokenDataStore.userName
    val userEmail = tokenDataStore.userEmail
    val role      = tokenDataStore.role
    val userId    = tokenDataStore.userId

    fun logout(onDone: () -> Unit) = viewModelScope.launch {
        authRepo.logout()
        onDone()
    }
}
