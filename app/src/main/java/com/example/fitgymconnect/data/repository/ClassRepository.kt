package com.example.fitgymconnect.data.repository

import com.example.fitgymconnect.data.model.GymClass
import com.example.fitgymconnect.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(private val api: ApiService) {

    suspend fun getClasses(): Result<List<GymClass>> {
        return try {
            val response = api.getClasses()
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error al cargar clases")
        } catch (e: Exception) {
            Result.Error("Error de conexión")
        }
    }
}
