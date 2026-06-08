package com.example.fitgymconnect.ui.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitgymconnect.data.model.ClassBookingItem
import com.example.fitgymconnect.data.model.ClassSchedule
import com.example.fitgymconnect.data.model.GymClass
import com.example.fitgymconnect.data.repository.ClassRepository
import com.example.fitgymconnect.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AgendaItem(
    val date: LocalDate,
    val gymClass: GymClass,
    val schedule: ClassSchedule,
    val bookings: List<ClassBookingItem>
)

@HiltViewModel
class TrainerAgendaViewModel @Inject constructor(
    private val repo: ClassRepository
) : ViewModel() {

    sealed class AgendaState {
        object Loading : AgendaState()
        data class Success(val items: List<AgendaItem>) : AgendaState()
        data class Error(val message: String) : AgendaState()
    }

    private val _state = MutableStateFlow<AgendaState>(AgendaState.Loading)
    val state: StateFlow<AgendaState> = _state

    fun loadAgenda(classes: List<GymClass>, userId: Int) = viewModelScope.launch {
        _state.value = AgendaState.Loading
        val myClasses = classes.filter { it.trainer?.user_id == userId }

        if (myClasses.isEmpty()) { _state.value = AgendaState.Success(emptyList()); return@launch }

        // Cargar reservas de todas las clases en paralelo
        val bookingsByClass: Map<Int, List<ClassBookingItem>> = myClasses
            .map { gymClass -> async { gymClass.id to fetchBookings(gymClass.id) } }
            .awaitAll()
            .toMap()

        // Generar ocurrencias para los próximos 14 días
        val today = LocalDate.now()
        val items = mutableListOf<AgendaItem>()

        for (offset in 0..13) {
            val date = today.plusDays(offset.toLong())
            val apiDow = date.dayOfWeek.value - 1  // 0=Lun..6=Dom

            myClasses.forEach { gymClass ->
                gymClass.schedules
                    ?.filter { it.dayOfWeek == apiDow }
                    ?.forEach { schedule ->
                        val allBookings = bookingsByClass[gymClass.id] ?: emptyList()
                        val sessionBookings = allBookings.filter { b ->
                            b.bookingDate?.take(10) == date.toString() &&
                            b.timeSlot?.take(5) == schedule.timeSlot.take(5)
                        }
                        items.add(AgendaItem(date, gymClass, schedule, sessionBookings))
                    }
            }
        }

        _state.value = AgendaState.Success(
            items.sortedWith(compareBy({ it.date }, { it.schedule.timeSlot }))
        )
    }

    private suspend fun fetchBookings(classId: Int): List<ClassBookingItem> =
        when (val r = repo.getClassBookings(classId)) {
            is Result.Success -> r.data
            is Result.Error   -> emptyList()
        }
}
