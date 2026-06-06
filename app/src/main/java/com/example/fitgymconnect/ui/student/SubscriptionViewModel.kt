package com.example.fitgymconnect.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.model.Subscription
import com.example.fitgymconnect.data.repository.Result
import com.example.fitgymconnect.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SubscriptionUiState {
    object Loading : SubscriptionUiState()
    object NoSubscription : SubscriptionUiState()
    data class Active(val subscription: Subscription) : SubscriptionUiState()
    data class Error(val message: String) : SubscriptionUiState()
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repo: SubscriptionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
    val state: StateFlow<SubscriptionUiState> = _state

    private val _clientSecret = MutableStateFlow<String?>(null)
    val clientSecret: StateFlow<String?> = _clientSecret

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init { loadSubscription() }

    fun loadSubscription() = viewModelScope.launch {
        _state.value = SubscriptionUiState.Loading
        _state.value = when (val r = repo.getSubscription()) {
            is Result.Success -> if (r.data != null && r.data.id > 0) SubscriptionUiState.Active(r.data) else SubscriptionUiState.NoSubscription
            is Result.Error   -> SubscriptionUiState.Error(r.message)
        }
    }

    fun initiatePayment() = viewModelScope.launch {
        _isLoading.value = true
        when (val r = repo.createPaymentIntent()) {
            is Result.Success -> _clientSecret.value = r.data.client_secret
            is Result.Error   -> _message.value = r.message
        }
        _isLoading.value = false
    }

    fun onPaymentSuccess(clientSecret: String) = viewModelScope.launch {
        val paymentIntentId = clientSecret.substringBefore("_secret_")
        _isLoading.value = true
        when (val r = repo.confirmSubscription(paymentIntentId)) {
            is Result.Success -> {
                _state.value = SubscriptionUiState.Active(r.data)
                _message.value = "¡Suscripción activada!"
            }
            is Result.Error -> _message.value = r.message
        }
        _isLoading.value = false
        _clientSecret.value = null
    }

    fun onPaymentCanceled() { _clientSecret.value = null }
    fun onPaymentFailed(error: String) { _message.value = "Error en el pago: $error"; _clientSecret.value = null }
    fun clearMessage() { _message.value = null }

    fun cancelSubscription() = viewModelScope.launch {
        _isLoading.value = true
        when (repo.cancelSubscription()) {
            is Result.Success -> { _state.value = SubscriptionUiState.NoSubscription; _message.value = "Suscripción cancelada" }
            is Result.Error   -> _message.value = "Error al cancelar"
        }
        _isLoading.value = false
    }
}
