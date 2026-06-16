package com.example.tarik.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarik.TarikApplication
import com.example.tarik.data.settings.SettingsRepository
import com.example.tarik.notifications.NotificationScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: SettingsRepository =
        (application as TarikApplication).settingsRepository

    // we keep a reference to the Application context for the scheduler
    private val appContext = application

    val darkMode: StateFlow<Boolean> = repo.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notificationTime: StateFlow<String> = repo.notificationTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "09:00")

    val categoryWeights: StateFlow<Map<String, Float>> = repo.categoryWeights.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repo.setDarkMode(enabled) }
    }

    fun setNotificationTime(time: String) {
        viewModelScope.launch {
            repo.setNotificationTime(time)

            // reschedule the alarm with the new time 9 am is our fallback
            val parts = time.split(":").mapNotNull { it.toIntOrNull() }
            val hour = parts.getOrNull(0) ?: 9
            val minute = parts.getOrNull(1) ?: 0

            NotificationScheduler.scheduleDailyNotification(appContext, hour, minute)
        }
    }

    fun setCategoryWeight(category: String, weight: Float) {
        viewModelScope.launch { repo.setCategoryWeight(category, weight) }
    }
}
