package com.example.smarttodotracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val notes: String = "",
    val isDone: Boolean = false,
    val dueAtMillis: Long? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val radiusMeters: Float? = null,
    val priority: Int? = null
)
