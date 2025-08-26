package com.example.smarttodotracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast // Added for Toast messages
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarttodotracker.data.Task
import com.example.smarttodotracker.databinding.ActivityMainBinding
import com.example.smarttodotracker.ui.TaskAdapter
import com.example.smarttodotracker.ui.TaskViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.Manifest
import com.example.smarttodotracker.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val backgroundLocationGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false

            if (fineLocationGranted && backgroundLocationGranted) {
                Snackbar.make(binding.root, "Location permissions granted.", Snackbar.LENGTH_SHORT).show()
                // You might want to re-evaluate geofences here if they depend on permissions
            } else {
                Snackbar.make(binding.root, "Location permissions denied. Geofencing may not work.", Snackbar.LENGTH_LONG).show()
            }
        }

        checkAndRequestLocationPermissions()

        // Set up RecyclerView with TaskAdapter
        taskAdapter = TaskAdapter(
            onItemClicked = { task ->
                showEditTaskDialog(task)
            },
            onDeleteClicked = { task ->
                taskViewModel.deleteTask(task.id)
                Toast.makeText(this, "Task '${task.title}' deleted.", Toast.LENGTH_SHORT).show()

            }
        )
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }

        // Observe tasks from ViewModel and submit to adapter
        lifecycleScope.launch {
            taskViewModel.tasks.collectLatest { tasks ->
                taskAdapter.submitList(tasks)
            }
        }

        // Set up FloatingActionButton click listener to show AddTaskDialog
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun checkAndRequestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // ACCESS_BACKGROUND_LOCATION is required for geofencing on Android 10 (API 29) and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.edit_task_title)
        val notesEditText = dialogView.findViewById<EditText>(R.id.edit_task_notes)
        val latEditText = dialogView.findViewById<EditText>(R.id.edit_task_latitude)
        val lngEditText = dialogView.findViewById<EditText>(R.id.edit_task_longitude)
        val radiusEditText = dialogView.findViewById<EditText>(R.id.edit_task_radius)
        val dueDateEditText = dialogView.findViewById<EditText>(R.id.edit_task_due_date)

        var selectedCalendar: Calendar? = null

        dueDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dueDateEditText.setText(dateFormat.format(selectedCalendar!!.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val title = titleEditText.text.toString()
                val notes = notesEditText.text.toString()

                val lat = latEditText.text.toString().toDoubleOrNull()
                val lng = lngEditText.text.toString().toDoubleOrNull()
                val radius = radiusEditText.text.toString().toFloatOrNull()
                val dueAtMillis = selectedCalendar?.timeInMillis

                if (title.isNotBlank()) {
                    taskViewModel.addTask(title, notes, lat, lng, radius, dueAtMillis)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_task, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.edit_task_title)
        val notesEditText = dialogView.findViewById<EditText>(R.id.edit_task_notes)
        val latEditText = dialogView.findViewById<EditText>(R.id.edit_task_latitude)
        val lngEditText = dialogView.findViewById<EditText>(R.id.edit_task_longitude)
        val radiusEditText = dialogView.findViewById<EditText>(R.id.edit_task_radius)
        val dueDateEditText = dialogView.findViewById<EditText>(R.id.edit_task_due_date)

        // Pre-populate fields with existing task data
        titleEditText.setText(task.title)
        notesEditText.setText(task.notes)
        latEditText.setText(task.lat?.toString())
        lngEditText.setText(task.lng?.toString())
        radiusEditText.setText(task.radiusMeters?.toString())

        var selectedCalendar: Calendar? = task.dueAtMillis?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }

        selectedCalendar?.let {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dueDateEditText.setText(dateFormat.format(it.time))
        }

        dueDateEditText.setOnClickListener {
            val calendar = selectedCalendar ?: Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dueDateEditText.setText(dateFormat.format(selectedCalendar!!.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val updatedTitle = titleEditText.text.toString()
                val updatedNotes = notesEditText.text.toString()

                val updatedLat = latEditText.text.toString().toDoubleOrNull()
                val updatedLng = lngEditText.text.toString().toDoubleOrNull()
                val updatedRadius = radiusEditText.text.toString().toFloatOrNull()
                val updatedDueAtMillis = selectedCalendar?.timeInMillis

                if (updatedTitle.isNotBlank()) {
                    val updatedTask = task.copy(
                        title = updatedTitle,
                        notes = updatedNotes,
                        lat = updatedLat,
                        lng = updatedLng,
                        radiusMeters = updatedRadius,
                        dueAtMillis = updatedDueAtMillis
                    )
                    taskViewModel.updateTask(updatedTask)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
