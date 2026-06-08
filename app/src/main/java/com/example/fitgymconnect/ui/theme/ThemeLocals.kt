package com.example.fitgymconnect.ui.theme

import androidx.compose.runtime.compositionLocalOf

val LocalIsDarkTheme = compositionLocalOf { false }
val LocalToggleDarkTheme = compositionLocalOf<() -> Unit> { {} }
