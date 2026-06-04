package com.example.fitgymconnect.data.network

import com.example.fitgymconnect.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/me")
    suspend fun me(): Response<User>

    // Content (public)
    @GET("api/routines")
    suspend fun getRoutines(): Response<List<Routine>>

    @GET("api/classes")
    suspend fun getClasses(): Response<List<GymClass>>

    // Bookings (authenticated via interceptor)
    @GET("api/bookings")
    suspend fun getBookings(): Response<List<Booking>>

    @POST("api/bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<Booking>

    @DELETE("api/bookings/{id}")
    suspend fun cancelBooking(@Path("id") id: Int): Response<Unit>

    // Subscription (authenticated via interceptor)
    @GET("api/subscription")
    suspend fun getSubscription(): Response<Subscription>

    @POST("api/subscription/payment-intent")
    suspend fun createPaymentIntent(): Response<PaymentIntentResponse>

    @POST("api/subscription")
    suspend fun confirmSubscription(@Body request: SubscriptionRequest): Response<Subscription>

    @POST("api/subscription/cancel")
    suspend fun cancelSubscription(): Response<Unit>
}
