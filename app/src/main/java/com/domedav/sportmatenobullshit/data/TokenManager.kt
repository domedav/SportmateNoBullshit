package com.domedav.sportmatenobullshit.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
    }

    // Get token as a Flow (updates automatically)
    val userToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_TOKEN_KEY]
    }

    // Save token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = token
        }
    }

    // Clear token (optional, for logout)
    suspend fun clearToken() {
        context.dataStore.edit { it.remove(USER_TOKEN_KEY) }
    }
}
