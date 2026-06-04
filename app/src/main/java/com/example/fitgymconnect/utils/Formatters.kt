package com.example.fitgymconnect.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun formatScheduledAt(dateStr: String): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("EEE d MMM, HH:mm", Locale("es", "ES"))
        output.format(input.parse(dateStr)!!)
    } catch (e: Exception) { dateStr }
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
        try {
            return output.format(SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)!!)
        } catch (_: Exception) {}
    }
    return dateStr
}
