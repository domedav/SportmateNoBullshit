package com.domedav.sportmatenobullshit.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        private val KEY_LAST_TAB = intPreferencesKey("last_tab_index")
    }

    suspend fun saveLastTab(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_TAB] = index
        }
    }

    fun getLastTab(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_LAST_TAB] ?: 0 // Default: 0 (Webview)
        }
    }
}
