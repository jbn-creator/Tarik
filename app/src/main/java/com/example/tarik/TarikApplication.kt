package com.example.tarik

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import coil.Coil
import com.example.tarik.data.HistoryImageModule
import com.example.tarik.data.HistoryRepository
import com.example.tarik.data.classifier.BayesianClassifier
import com.example.tarik.data.classifier.TrainingData
import com.example.tarik.data.database.HistoryDatabase
import com.example.tarik.data.settings.SettingsRepository
import com.example.tarik.notifications.TarikNotificationReceiver


class TarikApplication : Application() {

    val database: HistoryDatabase by lazy { HistoryDatabase.getDatabase(this) }

    // classifier trains once on first access from the bundled corpus
    val classifier: BayesianClassifier by lazy {
        BayesianClassifier().also {
            try {
                val examples = TrainingData.load(this)
                it.train(examples)
                Log.d("TarikApplication", "Classifier trained on ${examples.size} examples")
            } catch (e: Exception) {
                Log.e("TarikApplication", "Classifier training failed: ${e.message}")
            }
        }
    }

    val repository: HistoryRepository by lazy {
        HistoryRepository(database.historyDao(), classifier)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }

    override fun onCreate() {
        super.onCreate()

        val imageLoader = HistoryImageModule.createImageLoader(this)
        Coil.setImageLoader(imageLoader)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TarikNotificationReceiver.CHANNEL_ID,
            TarikNotificationReceiver.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminder about historical events from Tarik"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

    }
}