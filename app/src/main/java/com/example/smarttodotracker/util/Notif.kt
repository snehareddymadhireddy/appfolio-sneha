package com.example.smarttodotracker.util


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smarttodotracker.R

object Notif {
    const val CHANNEL_ID = "smarttodo_channel"

    fun ensureChannel(mgr: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Smart To-Do", NotificationManager.IMPORTANCE_DEFAULT)
            mgr.createNotificationChannel(ch)
        }
    }

    fun builder(ctx: Context, title: String, text: String): NotificationCompat.Builder =
        NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)  // add a vector drawable named ic_notification
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
}
