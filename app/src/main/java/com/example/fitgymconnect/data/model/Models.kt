package com.example.fitgymconnect.data.model

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class AuthResponse(val token: String, val user: User)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val email_verified_at: String?,
    val active: Boolean
)

data class Routine(
    val id: Int,
    val title: String,
    val description: String?,
    val difficulty: String?,
    val duration: Int?,
    val video_url: String?,
    val is_premium: Boolean?,
    val trainer_id: Int?,
    val trainer: TrainerProfile?
)

data class GymClass(
    val id: Int,
    val title: String,
    val description: String?,
    val type: String?,
    val scheduled_at: String?,
    val max_capacity: Int?,
    val price: Double?,
    val meet_link: String?,
    val trainer_id: Int?,
    val trainer: TrainerProfile?,
    val bookings_count: Int?
)

data class TrainerProfile(
    val id: Int,
    val user_id: Int?,
    val bio: String?,
    val specialty: String?,
    val photo: String?,
    val user: User?
)

data class BookingRequest(val class_id: Int)

data class Booking(
    val id: Int,
    val user_id: Int,
    val class_id: Int,
    val status: String,
    val gym_class: GymClass?
)

data class Subscription(
    val id: Int,
    val user_id: Int?,
    val stripe_payment_intent_id: String?,
    val starts_at: String?,
    val ends_at: String?,
    val status: String?
)

data class PaymentIntentResponse(val client_secret: String)

data class SubscriptionRequest(val payment_intent_id: String)
