package com.example.fitgymconnect.data.repository

import com.example.fitgymconnect.data.model.Routine
import com.example.fitgymconnect.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(private val api: ApiService) {

    suspend fun getRoutines(): Result<List<Routine>> {
        return try {
            val response = api.getRoutines()
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error al cargar rutinas")
        } catch (e: Exception) {
            Result.Error("Error de conexión")
        }
    }
}
