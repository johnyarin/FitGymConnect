package com.example.fitgymconnect.data.repository

import com.example.fitgymconnect.data.model.ClassAvailability
import com.example.fitgymconnect.data.model.GymClass
import com.example.fitgymconnect.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(private val api: ApiService) {

    suspend fun getClasses(): Result<List<GymClass>> = try {
        val r = api.getClasses()
        if (r.isSuccessful) Result.Success(r.body() ?: emptyList())
        else Result.Error("Error al cargar clases")
    } catch (e: Exception) { Result.Error("Error de conexión") }

    suspend fun getClassBookings(classId: Int): Result<List<com.example.fitgymconnect.data.model.ClassBookingItem>> = try {
        val r = api.getClassBookings(classId)
        if (r.isSuccessful) Result.Success(r.body() ?: emptyList())
        else Result.Error("Error al cargar alumnos")
    } catch (e: Exception) { Result.Error("Error de conexión") }

    suspend fun getClassAvailability(classId: Int, date: String, timeSlot: String): Result<ClassAvailability> = try {
        val r = api.getClassAvailability(classId, date, timeSlot)
        if (r.isSuccessful) Result.Success(r.body()!!)
        else Result.Error("Error al consultar disponibilidad")
    } catch (e: Exception) { Result.Error("Error de conexión") }
}
