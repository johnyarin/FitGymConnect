package com.example.fitgymconnect.data.repository

import com.example.fitgymconnect.data.model.Booking
import com.example.fitgymconnect.data.model.BookingRequest
import com.example.fitgymconnect.data.network.ApiService
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(private val api: ApiService) {

    suspend fun getBookings(): Result<List<Booking>> = try {
        val r = api.getBookings()
        if (r.isSuccessful) Result.Success(r.body() ?: emptyList())
        else Result.Error("Error al cargar reservas")
    } catch (e: Exception) { Result.Error("Error de conexión") }

    suspend fun bookClass(classId: Int): Result<Booking> = try {
        val r = api.createBooking(BookingRequest(class_id = classId))
        if (r.isSuccessful) Result.Success(r.body()!!)
        else Result.Error(parseError(r.errorBody()?.string()))
    } catch (e: Exception) { Result.Error("Error de conexión") }

    suspend fun cancelBooking(bookingId: Int): Result<Unit> = try {
        val r = api.cancelBooking(bookingId)
        if (r.isSuccessful) Result.Success(Unit)
        else Result.Error("Error al cancelar la reserva")
    } catch (e: Exception) { Result.Error("Error de conexión") }

    private fun parseError(body: String?): String = try {
        JSONObject(body ?: "{}").getString("message")
    } catch (e: Exception) { "No se pudo realizar la reserva" }
}
