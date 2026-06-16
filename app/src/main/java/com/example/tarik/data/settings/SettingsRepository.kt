package com.example.tarik.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// extension on Context that creates a single Preference DataStore
private val Context.dataStore by preferencesDataStore(name = "tarik_settings")


class SettingsRepository(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATION_TIME = stringPreferencesKey("notification_time")

        // one weight per classifier category stored as floats 0.0 to 1.0
        val WEIGHT_WAR = floatPreferencesKey("weight_war")
        val WEIGHT_SCIENCE = floatPreferencesKey("weight_science")
        val WEIGHT_POLITICS = floatPreferencesKey("weight_politics")
        val WEIGHT_SPORTS = floatPreferencesKey("weight_sports")
        val WEIGHT_RELIGION = floatPreferencesKey("weight_religion")
        val WEIGHT_GENERAL = floatPreferencesKey("weight_general")
    }

    // Flows that emit the current value and re-emit whenever the value changes

    val darkMode: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.DARK_MODE] ?: false }

    val notificationTime: Flow<String> = context.dataStore.data
        .map { it[Keys.NOTIFICATION_TIME] ?: "09:00" }

    // category weights default to 0.5 which is neutral this way the user can tune up or down
    val categoryWeights: Flow<Map<String, Float>> = context.dataStore.data.map { prefs ->
        mapOf(
            "War" to (prefs[Keys.WEIGHT_WAR] ?: 0.5f),
            "Science" to (prefs[Keys.WEIGHT_SCIENCE] ?: 0.5f),
            "Politics" to (prefs[Keys.WEIGHT_POLITICS] ?: 0.5f),
            "Sports" to (prefs[Keys.WEIGHT_SPORTS] ?: 0.5f),
            "Religion" to (prefs[Keys.WEIGHT_RELIGION] ?: 0.5f),
            "General" to (prefs[Keys.WEIGHT_GENERAL] ?: 0.5f)
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setNotificationTime(time: String) {
        context.dataStore.edit { it[Keys.NOTIFICATION_TIME] = time }
    }

    suspend fun setCategoryWeight(category: String, weight: Float) {
        val key = when (category) {
            "War" -> Keys.WEIGHT_WAR
            "Science" -> Keys.WEIGHT_SCIENCE
            "Politics" -> Keys.WEIGHT_POLITICS
            "Sports" -> Keys.WEIGHT_SPORTS
            "Religion" -> Keys.WEIGHT_RELIGION
            "General" -> Keys.WEIGHT_GENERAL
            else -> return
        }
        context.dataStore.edit { it[key] = weight }
    }
}