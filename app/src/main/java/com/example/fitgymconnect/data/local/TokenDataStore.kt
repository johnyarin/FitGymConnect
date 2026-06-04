package com.example.fitgymconnect.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TOKEN_KEY      = stringPreferencesKey("token")
    private val ROLE_KEY       = stringPreferencesKey("role")
    private val USER_ID_KEY    = intPreferencesKey("user_id")
    private val USER_NAME_KEY  = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")

    val token:     Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val role:      Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }
    val userId:    Flow<Int?>    = context.dataStore.data.map { it[USER_ID_KEY] }
    val userName:  Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL_KEY] }

    suspend fun saveSession(token: String, role: String, userId: Int, userName: String, userEmail: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY]      = token
            prefs[ROLE_KEY]       = role
            prefs[USER_ID_KEY]    = userId
            prefs[USER_NAME_KEY]  = userName
            prefs[USER_EMAIL_KEY] = userEmail
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
