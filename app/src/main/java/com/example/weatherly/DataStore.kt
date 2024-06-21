package com.example.weatherly

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

class DataStore(private val context: Context) {
    private val darkMode = booleanPreferencesKey("dark_mode")

    // store dark mode value
    suspend fun writeTheme(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[darkMode] = value
        }
    }

    // get dark mode value
    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[darkMode] == true
    }
}