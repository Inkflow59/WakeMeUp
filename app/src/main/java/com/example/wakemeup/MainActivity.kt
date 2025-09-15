package com.example.wakemeup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wakemeup.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var alarmAdapter: AlarmAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeLocationService()
        } else {
            Toast.makeText(this, "Les permissions de localisation sont nécessaires", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupButtons()

        checkPermissions()
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onToggleAlarm = { alarm -> viewModel.toggleAlarm(alarm) },
            onDeleteAlarm = { alarm -> viewModel.deleteAlarm(alarm) },
            onEditAlarm = { alarm -> openAlarmEditor(alarm) }
        )

        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = alarmAdapter
        }
    }

    private fun setupObservers() {
        viewModel.alarms.observe(this) { alarms ->
            alarmAdapter.submitList(alarms)
            binding.textViewNoAlarms.visibility = if (alarms.isEmpty())
                android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun setupButtons() {
        binding.fabAddAlarm.setOnClickListener {
            openAlarmEditor(null)
        }
    }

    private fun checkPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Permission d'arrière-plan pour Android 10+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        // Permission de notification pour Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            initializeLocationService()
        }
    }

    private fun initializeLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun openAlarmEditor(alarm: LocationAlarm?) {
        val intent = Intent(this, AlarmEditorActivity::class.java)
        alarm?.let { intent.putExtra("alarm", it) }
        startActivity(intent)
    }
}
