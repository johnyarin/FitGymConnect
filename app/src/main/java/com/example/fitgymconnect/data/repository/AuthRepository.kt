package com.example.fitgymconnect.data.repository

import com.example.fitgymconnect.data.local.TokenDataStore
import com.example.fitgymconnect.data.model.LoginRequest
import com.example.fitgymconnect.data.model.RegisterRequest
import com.example.fitgymconnect.data.model.User
import com.example.fitgymconnect.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

data class AuthResult(
    val token: String,
    val role: String,
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val emailVerified: Boolean = true
)

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                val result = AuthResult(
                    body.token, body.user.role, body.user.id, body.user.name, body.user.email,
                    body.user.email_verified_at != null
                )
                tokenDataStore.saveSession(result.token, result.role, result.userId, result.userName, result.userEmail)
                Result.Success(result)
            } else {
                Result.Error("Credenciales incorrectas")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión")
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<AuthResult> {
        return try {
            val response = api.register(RegisterRequest(name, email, password, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                val result = AuthResult(
                    body.token, body.user.role, body.user.id, body.user.name, body.user.email,
                    body.user.email_verified_at != null
                )
                tokenDataStore.saveSession(result.token, result.role, result.userId, result.userName, result.userEmail)
                Result.Success(result)
            } else {
                Result.Error("No se pudo crear la cuenta")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión")
        }
    }

    suspend fun resendVerification(): Result<Unit> {
        return try {
            val response = api.resendVerification()
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("No se pudo reenviar el email")
        } catch (e: Exception) {
            Result.Error("Error de conexión")
        }
    }

    suspend fun checkVerification(): Result<User> {
        return try {
            val response = api.me()
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error al verificar")
        } catch (e: Exception) {
            Result.Error("Error de conexión")
        }
    }

    suspend fun logout() {
        try { api.logout() } catch (_: Exception) {}
        tokenDataStore.clearSession()
    }
}
