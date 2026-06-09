package com.example.fitgymconnect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary              = OrangeLight,         // #FFB59A — legible sobre fondos oscuros
    onPrimary            = Color(0xFF5C1900),
    primaryContainer     = OrangeDark,          // #832800
    onPrimaryContainer   = OrangeContainer,     // #FFDBCC
    secondary            = LightGray,           // #E8E8E8
    onSecondary          = NearBlack,
    secondaryContainer   = DarkGray,            // #3D3D3D
    onSecondaryContainer = LightGray,
    tertiary             = OrangePrimary,
    onTertiary           = Color.White,
    background           = DarkBackground,      // #1A1A1A
    onBackground         = LightGray,
    surface              = DarkSurface,         // #242424
    onSurface            = LightGray,
    surfaceVariant       = DarkSurfaceVariant,  // #2E2E2E
    onSurfaceVariant     = MidGray,
    outline              = DarkGray,
)

private val LightColorScheme = lightColorScheme(
    primary              = OrangePrimary,       // #FF6200
    onPrimary            = Color.White,
    primaryContainer     = OrangeContainer,     // #FFDBCC
    onPrimaryContainer   = Color(0xFF3A1100),
    secondary            = NearBlack,           // #212121
    onSecondary          = Color.White,
    secondaryContainer   = VeryLightGray,       // #F5F5F5
    onSecondaryContainer = NearBlack,
    tertiary             = OrangeDark,
    onTertiary           = Color.White,
    background           = Color.White,
    onBackground         = NearBlack,
    surface              = Color.White,
    onSurface            = NearBlack,
    surfaceVariant       = VeryLightGray,
    onSurfaceVariant     = MidGray,
    outline              = LightGray,
)

@Composable
fun FitGymConnectTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
