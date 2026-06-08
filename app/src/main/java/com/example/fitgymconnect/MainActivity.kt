package com.example.fitgymconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.fitgymconnect.navigation.NavGraph
import com.example.fitgymconnect.ui.theme.FitGymConnectTheme
import com.example.fitgymconnect.ui.theme.LocalIsDarkTheme
import com.example.fitgymconnect.ui.theme.LocalToggleDarkTheme
import com.example.fitgymconnect.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            CompositionLocalProvider(
                LocalIsDarkTheme provides isDarkTheme,
                LocalToggleDarkTheme provides { themeViewModel.toggleTheme() }
            ) {
                FitGymConnectTheme(darkTheme = isDarkTheme) {
                    NavGraph()
                }
            }
        }
    }
}
