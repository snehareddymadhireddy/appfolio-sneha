package com.example.smarttodotracker.geofencing

import android.Manifest
import android.app.NotificationManager // Import NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import com.example.smarttodotracker.util.Notif

class GeofenceReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        // Get NotificationManager system service
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Notif.ensureChannel(notificationManager) // Pass the NotificationManager instance

        val n = Notif.builder(context, "Nearby task", "You're near a task location").build()
        // Use NotificationManagerCompat for notifying to ensure compatibility
        NotificationManagerCompat.from(context).notify((Math.random()*100000).toInt(), n)
    }
}
