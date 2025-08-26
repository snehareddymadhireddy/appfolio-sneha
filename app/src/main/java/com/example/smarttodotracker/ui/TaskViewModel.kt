package com.example.smarttodotracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttodotracker.data.Task
import com.example.smarttodotracker.data.TaskRepository
import com.example.smarttodotracker.geofencing.GeofenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = taskRepository.getTasks()
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, notes: String = "", lat: Double? = null, lng: Double? = null, radiusMeters: Float? = null) {
        viewModelScope.launch {
            val newTask = Task(title = title, notes = notes, lat = lat, lng = lng, radiusMeters = radiusMeters)
            val newTaskId = taskRepository.addTask(newTask) // TaskDao.insert returns Long now
            val taskWithId = newTask.copy(id = newTaskId)
            if (taskWithId.lat != null && taskWithId.lng != null && taskWithId.radiusMeters != null) {
                geofenceManager.addGeofence(taskWithId)
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            val oldTask = taskRepository.getTaskById(task.id) // Get old task to compare location
            taskRepository.updateTask(task)

            // Check for geofence changes
            val oldLocationExists = oldTask?.lat != null && oldTask.lng != null && oldTask.radiusMeters != null
            val newLocationExists = task.lat != null && task.lng != null && task.radiusMeters != null

            if (oldLocationExists && !newLocationExists) {
                // Location removed, remove geofence
                geofenceManager.removeGeofence(task.id)
            } else if (!oldLocationExists && newLocationExists) {
                // Location added, add geofence
                geofenceManager.addGeofence(task)
            } else if (oldLocationExists && newLocationExists) {
                // Location changed, remove old and add new
                if (oldTask?.lat != task.lat || oldTask.lng != task.lng || oldTask.radiusMeters != task.radiusMeters) {
                    geofenceManager.removeGeofence(task.id)
                    geofenceManager.addGeofence(task)
                }
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            val taskToDelete = taskRepository.getTaskById(taskId)
            if (taskToDelete != null && taskToDelete.lat != null && taskToDelete.lng != null && taskToDelete.radiusMeters != null) {
                geofenceManager.removeGeofence(taskId)
            }
            taskRepository.deleteTask(taskId)
        }
    }
}
