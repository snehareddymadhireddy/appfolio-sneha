package com.example.smarttodotracker.di

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.smarttodotracker.data.AppDatabase
import com.example.smarttodotracker.data.TaskDao
import com.example.smarttodotracker.data.TaskRepository // Correct import for the interface
import com.example.smarttodotracker.data.TaskRepositoryImpl // Import the concrete implementation
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "smarttodo.db").build()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepositoryImpl(taskDao) // Provide the concrete implementation TaskRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideGeofencingClient(@ApplicationContext ctx: Context): GeofencingClient =
        LocationServices.getGeofencingClient(ctx)

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext ctx: Context): NotificationManager =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext ctx: Context): WorkManager =
        WorkManager.getInstance(ctx)
}