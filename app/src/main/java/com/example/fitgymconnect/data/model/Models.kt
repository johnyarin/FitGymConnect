package com.example.fitgymconnect.data.model

import com.google.gson.annotations.SerializedName

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

data class Exercise(
    val id: Int,
    val name: String,
    val description: String?,
    val sets: Int?,
    val duration_seconds: Int?,
    val rest_seconds: Int?,
    val order: Int,
    val image_url: String?,
    val video_url: String?
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
    val trainer: TrainerProfile?,
    val exercises: List<Exercise>? = null
)

data class ClassSchedule(
    val id: Int,
    @SerializedName("day_of_week")  val dayOfWeek: Int,    // 0=Lun .. 6=Dom
    @SerializedName("time_slot")    val timeSlot: String,   // "09:00:00"
    @SerializedName("max_students") val maxStudents: Int
)

data class GymClass(
    val id: Int,
    val title: String,
    val description: String?,
    val type: String?,        // "online" | "presential"
    val meet_link: String?,
    val trainer_id: Int?,
    val trainer: TrainerProfile?,
    val schedules: List<ClassSchedule>? = null
)

data class TrainerProfile(
    val id: Int,
    val user_id: Int?,
    val bio: String?,
    @SerializedName("speciality") val specialty: String?,
    val photo: String?,
    val user: User?
)

data class ClassAvailability(
    @SerializedName("available_spots") val availableSpots: Int,
    @SerializedName("max_students")    val maxStudents: Int,
    val booked: Int,
    @SerializedName("is_full") val isFull: Boolean
)

data class BookingRequest(
    val class_id: Int,
    val booking_date: String,
    val time_slot: String
)

data class Booking(
    val id: Int,
    val user_id: Int,
    val class_id: Int,
    val booking_date: String?,
    val time_slot: String?,
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

data class ClassBookingItem(
    val id: Int,
    @SerializedName("booking_date") val bookingDate: String?,
    @SerializedName("time_slot")    val timeSlot: String?,
    val status: String,
    val user: User?
)

data class PaymentIntentResponse(val client_secret: String)

data class SubscriptionRequest(val payment_intent_id: String)
