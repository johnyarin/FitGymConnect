package com.example.fitgymconnect.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme")
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = context.themeDataStore.data
        .map { prefs -> prefs[DARK_MODE_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleTheme() = viewModelScope.launch {
        context.themeDataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = !(prefs[DARK_MODE_KEY] ?: false)
        }
    }
}
