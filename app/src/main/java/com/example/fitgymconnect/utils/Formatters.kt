package com.example.fitgymconnect.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun formatScheduledAt(dateStr: String): String {
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )
    val output = SimpleDateFormat("EEE d MMM, HH:mm", Locale("es", "ES"))
    for (fmt in formats) {
        try { return output.format(SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)!!) }
        catch (_: Exception) {}
    }
    return dateStr
}

fun difficultyLabel(diff: String) = when (diff) {
    "beginner"     -> "Principiante"
    "intermediate" -> "Intermedio"
    "advanced"     -> "Avanzado"
    else           -> diff
}

fun statusLabel(status: String) = when (status) {
    "pending"   -> "Pendiente"
    "confirmed" -> "Confirmada"
    "cancelled" -> "Cancelada"
    else        -> status
}

fun formatEndDate(dateStr: String): String {
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )
    val output = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
    for (fmt in formats) {
        try { return output.format(SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)!!) }
        catch (_: Exception) {}
    }
    return dateStr
}

// 0=Lun .. 6=Dom
fun dayOfWeekLabel(dow: Int) = when (dow) {
    0 -> "Lun"; 1 -> "Mar"; 2 -> "Mié"
    3 -> "Jue"; 4 -> "Vie"; 5 -> "Sáb"
    6 -> "Dom"; else -> ""
}

fun dayOfWeekFull(dow: Int) = when (dow) {
    0 -> "Lunes"; 1 -> "Martes"; 2 -> "Miércoles"
    3 -> "Jueves"; 4 -> "Viernes"; 5 -> "Sábado"
    6 -> "Domingo"; else -> ""
}

// "09:00:00" → "09:00"
fun formatTimeSlot(timeSlot: String) = timeSlot.take(5)

// "2026-06-10" + "09:00:00" → "Mar 10 Jun, 09:00"
fun formatBookingDateTime(date: String, timeSlot: String): String {
    val time = formatTimeSlot(timeSlot)
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("EEE d MMM", Locale("es", "ES"))
        "${output.format(input.parse(date)!!)}, $time"
    } catch (_: Exception) { "$date $time" }
}
