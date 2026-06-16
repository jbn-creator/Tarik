package com.example.tarik.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.tarik.MainActivity
import com.example.tarik.R


class TarikNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // this intent opens the app when the user taps the notification
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Today in History")
            .setContentText("New historical events are waiting for you in Tarik")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // we use not. id 1 to ensure only one today in history notification is visible at a time
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        // ensuring the channel id matches the one created in tarikApplication
        const val CHANNEL_ID = "tarik_daily_channel"
        const val CHANNEL_NAME = "Daily History Reminder"
        const val NOTIFICATION_ID = 1
    }
}
