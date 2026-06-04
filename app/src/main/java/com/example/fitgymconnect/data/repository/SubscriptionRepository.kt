package com.example.fitgymconnect.data.repository

import com.example.fitgymconnect.data.model.PaymentIntentResponse
import com.example.fitgymconnect.data.model.Subscription
import com.example.fitgymconnect.data.model.SubscriptionRequest
import com.example.fitgymconnect.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(private val api: ApiService) {

    suspend fun getSubscription(): Result<Subscription?> = try {
        val r = api.getSubscription()
        when {
            r.isSuccessful    -> Result.Success(r.body())
            r.code() == 404   -> Result.Success(null)
            else              -> Result.Error("Error al cargar suscripción")
        }
    } catch (e: Exception) { Result.Error("Error de conexión") }

    suspend fun createPaymentIntent(): Result<PaymentIntentResponse> = try {
        val r = api.createPaymentIntent()
        if (r.isSuccessful) Result.Success(r.body()!!)
        else Result.Error("Error al iniciar el pago")
    } catch (e: Exception) { Result.Error("Error de conexión") }

    suspend fun confirmSubscription(paymentIntentId: String): Result<Subscription> = try {
        val r = api.confirmSubscription(SubscriptionRequest(paymentIntentId))
        if (r.isSuccessful) Result.Success(r.body()!!)
        else Result.Error("Error al activar la suscripción")
    } catch (e: Exception) { Result.Error("Error de conexión") }

    suspend fun cancelSubscription(): Result<Unit> = try {
        val r = api.cancelSubscription()
        if (r.isSuccessful) Result.Success(Unit)
        else Result.Error("Error al cancelar la suscripción")
    } catch (e: Exception) { Result.Error("Error de conexión") }
}
