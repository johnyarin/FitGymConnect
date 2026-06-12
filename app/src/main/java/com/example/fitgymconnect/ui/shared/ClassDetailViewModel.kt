package com.example.fitgymconnect.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.model.ClassAvailability
import com.example.fitgymconnect.data.repository.ClassRepository
import com.example.fitgymconnect.data.repository.Result
import com.example.fitgymconnect.utils.formatTimeSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val repo: ClassRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _selectedTimeSlot = MutableStateFlow<String?>(null)
    val selectedTimeSlot: StateFlow<String?> = _selectedTimeSlot

    private val _availability = MutableStateFlow<ClassAvailability?>(null)
    val availability: StateFlow<ClassAvailability?> = _availability

    private val _isLoadingAvailability = MutableStateFlow(false)
    val isLoadingAvailability: StateFlow<Boolean> = _isLoadingAvailability

    fun selectDate(classId: Int, date: LocalDate) {
        _selectedDate.value = date
        _availability.value = null
        val slot = _selectedTimeSlot.value
        if (slot != null) loadAvailability(classId, date, slot)
    }

    fun selectTimeSlot(classId: Int, timeSlot: String) {
        _selectedTimeSlot.value = timeSlot
        _availability.value = null
        val date = _selectedDate.value
        if (date != null) loadAvailability(classId, date, timeSlot)
    }

    private fun loadAvailability(classId: Int, date: LocalDate, timeSlot: String) = viewModelScope.launch {
        _isLoadingAvailability.value = true
        when (val r = repo.getClassAvailability(classId, date.toString(), formatTimeSlot(timeSlot))) {
            is Result.Success -> _availability.value = r.data
            is Result.Error   -> _availability.value = null
        }
        _isLoadingAvailability.value = false
    }

    fun loadForSlot(classId: Int, date: LocalDate, timeSlot: String) {
        _selectedDate.value = date
        _selectedTimeSlot.value = timeSlot
        loadAvailability(classId, date, timeSlot)
    }

    fun reset() {
        _selectedDate.value = null
        _selectedTimeSlot.value = null
        _availability.value = null
    }
}
