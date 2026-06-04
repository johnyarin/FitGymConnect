package com.example.fitgymconnect.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.model.Booking
import com.example.fitgymconnect.data.repository.BookingRepository
import com.example.fitgymconnect.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BookingUiState {
    object Loading : BookingUiState()
    data class Success(val bookings: List<Booking>) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repo: BookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BookingUiState>(BookingUiState.Loading)
    val state: StateFlow<BookingUiState> = _state

    private val _processingIds = MutableStateFlow<Set<Int>>(emptySet())
    val processingIds: StateFlow<Set<Int>> = _processingIds

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init { loadBookings() }

    fun loadBookings() = viewModelScope.launch {
        _state.value = BookingUiState.Loading
        _state.value = when (val r = repo.getBookings()) {
            is Result.Success -> BookingUiState.Success(r.data)
            is Result.Error   -> BookingUiState.Error(r.message)
        }
    }

    fun bookClass(classId: Int) = viewModelScope.launch {
        _processingIds.value += classId
        when (val r = repo.bookClass(classId)) {
            is Result.Success -> { _message.value = "¡Reserva realizada!"; loadBookings() }
            is Result.Error   -> _message.value = r.message
        }
        _processingIds.value -= classId
    }

    fun cancelBooking(bookingId: Int, classId: Int) = viewModelScope.launch {
        _processingIds.value += classId
        when (val r = repo.cancelBooking(bookingId)) {
            is Result.Success -> { _message.value = "Reserva cancelada"; loadBookings() }
            is Result.Error   -> _message.value = r.message
        }
        _processingIds.value -= classId
    }

    fun clearMessage() { _message.value = null }

    fun getBookingForClass(classId: Int): Booking? =
        (state.value as? BookingUiState.Success)?.bookings
            ?.firstOrNull { it.class_id == classId && it.status != "cancelled" }
}
