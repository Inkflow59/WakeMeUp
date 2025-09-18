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
    private lateinit var permissionManager: PermissionManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager(this)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupButtons()
        setupStopAlarmButton()

        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        // Vérifier les permissions à chaque retour sur l'activité
        // (cas où l'utilisateur revient des paramètres)
        if (permissionManager.areAllPermissionsGranted()) {
            initializeLocationService()
            viewModel.refreshAlarms()

            // Vérifier l'optimisation de batterie (optionnel)
            if (!permissionManager.checkBatteryOptimization()) {
                permissionManager.showBatteryOptimizationDialog()
            }
        } else {
            // Si les permissions ne sont toujours pas accordées, redemander ou rediriger
            val missingPermissions = permissionManager.getMissingPermissions()
            val hasBeenAsked = missingPermissions.any { permission ->
                permissionManager.isPermissionPermanentlyDenied(permission)
            }

            if (hasBeenAsked) {
                // Redirection automatique vers les paramètres
                permissionManager.showPermissionSettingsDialog()
            }
        }
        updateStopAlarmButtonVisibility()
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
            if (permissionManager.areAllPermissionsGranted()) {
                openAlarmEditor(null)
            } else {
                Toast.makeText(this, "Les permissions de localisation sont requises pour créer une alarme", Toast.LENGTH_LONG).show()
                checkAndRequestPermissions()
            }
        }
    }

    private fun setupStopAlarmButton() {
        binding.stopAlarmButton.setOnClickListener {
            stopActiveAlarm()
        }
        updateStopAlarmButtonVisibility()
    }

    private fun updateStopAlarmButtonVisibility() {
        val prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE)
        val alarmActive = prefs.getBoolean("alarm_active", false)
        binding.stopAlarmButton.visibility = if (alarmActive) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun checkAndRequestPermissions() {
        if (permissionManager.areAllPermissionsGranted()) {
            initializeLocationService()
            return
        }

        val missingPermissions = permissionManager.getMissingPermissions()

        // Vérifier si certaines permissions ont été refusées de manière permanente
        val permanentlyDeniedPermissions = missingPermissions.filter { permission ->
            permissionManager.isPermissionPermanentlyDenied(permission)
        }

        if (permanentlyDeniedPermissions.isNotEmpty()) {
            // Redirection automatique vers les paramètres
            permissionManager.showPermissionSettingsDialog()
        } else {
            // Afficher l'explication puis demander les permissions
            permissionManager.showPermissionRationale {
                requestPermissionLauncher.launch(missingPermissions.toTypedArray())
            }
        }
    }

    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            initializeLocationService()
            Toast.makeText(this, "Permissions accordées ! L'application est prête à être utilisée.", Toast.LENGTH_SHORT).show()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys.toList()

            // Vérifier si des permissions ont été refusées de manière permanente
            val permanentlyDenied = deniedPermissions.any { permission ->
                permissionManager.isPermissionPermanentlyDenied(permission)
            }

            if (permanentlyDenied) {
                // Redirection automatique vers les paramètres
                permissionManager.showPermissionSettingsDialog()
            } else {
                // L'utilisateur a refusé mais peut encore accorder les permissions
                Toast.makeText(this, "Certaines permissions sont nécessaires pour le bon fonctionnement de l'application", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializeLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun openAlarmEditor(alarm: LocationAlarm?) {
        if (!permissionManager.areAllPermissionsGranted()) {
            Toast.makeText(this, "Les permissions de localisation sont requises", Toast.LENGTH_SHORT).show()
            checkAndRequestPermissions()
            return
        }

        val intent = Intent(this, AlarmEditorActivity::class.java)
        alarm?.let { intent.putExtra("alarm", it) }
        startActivity(intent)
    }

    private fun stopActiveAlarm() {
        val intent = Intent(this, LocationService::class.java).apply {
            action = "STOP_ALARM"
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        // Masquer le bouton immédiatement pour le retour visuel
        binding.stopAlarmButton.visibility = android.view.View.GONE
    }
}
