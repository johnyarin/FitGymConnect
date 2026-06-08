package com.example.fitgymconnect.ui.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.model.ClassAvailability
import com.example.fitgymconnect.data.model.GymClass
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
class TrainerOccupancyViewModel @Inject constructor(
    val repo: ClassRepository
) : ViewModel() {

    private val _occupancy = MutableStateFlow<Map<Int, ClassAvailability>>(emptyMap())
    val occupancy: StateFlow<Map<Int, ClassAvailability>> = _occupancy

    fun loadOccupancy(classes: List<GymClass>) = viewModelScope.launch {
        val result = mutableMapOf<Int, ClassAvailability>()
        val today = LocalDate.now()

        classes.forEach { gymClass ->
            gymClass.schedules?.forEach { schedule ->
                val todayDow = today.dayOfWeek.value - 1  // 0=Lun..6=Dom
                val daysUntil = (schedule.dayOfWeek - todayDow + 7) % 7
                val nextDate = if (daysUntil == 0) today else today.plusDays(daysUntil.toLong())

                when (val r = repo.getClassAvailability(gymClass.id, nextDate.toString(), formatTimeSlot(schedule.timeSlot))) {
                    is Result.Success -> result[schedule.id] = r.data
                    is Result.Error   -> {}
                }
            }
        }
        _occupancy.value = result
    }
}
