package com.example.smarttodotracker.geofencing

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.smarttodotracker.data.Task
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.GeofencingClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofencingClient: GeofencingClient
) {

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java).apply {
            action = "com.example.smarttodo.GEOFENCE_EVENT"
        }
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    @SuppressLint("MissingPermission")
    suspend fun addGeofence(task: Task) {
        if (task.lat == null || task.lng == null || task.radiusMeters == null) {
            return // Cannot add geofence without location details
        }

        val geofence = Geofence.Builder()
            .setRequestId(task.id.toString())
            .setCircularRegion(task.lat, task.lng, task.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()

        try {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).await()
            println("Geofence added for task: ${task.title}")
        } catch (e: Exception) {
            println("Error adding geofence: ${e.message}")
        }
    }

    suspend fun removeGeofence(taskId: Long) {
        try {
            geofencingClient.removeGeofences(listOf(taskId.toString())).await()
            println("Geofence removed for task ID: $taskId")
        } catch (e: Exception) {
            println("Error removing geofence: ${e.message}")
        }
    }

    suspend fun removeAllGeofences() {
        try {
            geofencingClient.removeGeofences(geofencePendingIntent).await()
            println("All geofences removed.")
        } catch (e: Exception) {
            println("Error removing all geofences: ${e.message}")
        }
    }
}
