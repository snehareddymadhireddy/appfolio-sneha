package com.example.smarttodotracker.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>
    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Long)
    suspend fun getTaskById(taskId: Long): Task?
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> = taskDao.observeTasks()

    override suspend fun addTask(task: Task): Long {
        return taskDao.insert(task)
    }

    override suspend fun updateTask(task: Task) {
        taskDao.update(task)
    }

    override suspend fun deleteTask(taskId: Long) {
        taskDao.delete(taskId)
    }

    override suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)
}