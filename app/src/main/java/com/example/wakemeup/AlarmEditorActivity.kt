package com.example.wakemeup

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.wakemeup.databinding.ActivityAlarmEditorBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*

class AlarmEditorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAlarmEditorBinding
    private lateinit var alarmRepository: AlarmRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var selectedLocation: LatLng? = null
    private var radiusCircle: Circle? = null
    private var editingAlarm: LocationAlarm? = null

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableMyLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmRepository = AlarmRepository(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Récupérer l'alarme à éditer si elle existe
        editingAlarm = intent.getParcelableExtra("alarm")

        setupUI()
        setupMap()
    }

    private fun setupUI() {
        // Pré-remplir les champs si on édite une alarme existante
        editingAlarm?.let { alarm ->
            binding.editTextAlarmName.setText(alarm.name)
            binding.seekBarRadius.progress = alarm.radius.toInt()
            binding.switchSound.isChecked = alarm.soundEnabled
            binding.switchVibration.isChecked = alarm.vibrationEnabled
            selectedLocation = LatLng(alarm.latitude, alarm.longitude)
        }

        // Configuration du slider de rayon
        binding.seekBarRadius.max = 1000 // Maximum 1km
        binding.seekBarRadius.progress = editingAlarm?.radius?.toInt() ?: 100
        updateRadiusText()

        binding.seekBarRadius.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                updateRadiusText()
                updateRadiusCircle()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // Boutons
        binding.buttonSave.setOnClickListener { saveAlarm() }
        binding.buttonCancel.setOnClickListener { finish() }
        binding.buttonCurrentLocation.setOnClickListener { getCurrentLocation() }
        binding.buttonSearchAddress.setOnClickListener { searchAddress() }

        // Titre
        title = if (editingAlarm == null) "Nouvelle alarme" else "Modifier l'alarme"
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configuration de la carte
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false

        // Clic sur la carte pour sélectionner une position
        map.setOnMapClickListener { latLng ->
            selectedLocation = latLng
            updateMapMarker()
            updateAddressFromLocation(latLng)
        }

        // Afficher la position initiale
        selectedLocation?.let { location ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            updateMapMarker()
        } ?: run {
            // Position par défaut (Paris)
            val defaultLocation = LatLng(48.8566, 2.3522)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
        }

        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    selectedLocation = latLng
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    updateMapMarker()
                    updateAddressFromLocation(latLng)
                }
            }
        }
    }

    private fun searchAddress() {
        val address = binding.editTextAddress.text.toString().trim()
        if (address.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une adresse", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)

            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)
                selectedLocation = latLng
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                updateMapMarker()
            } else {
                Toast.makeText(this, "Adresse non trouvée", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Erreur lors de la recherche d'adresse", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAddressFromLocation(latLng: LatLng) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
                binding.editTextAddress.setText(address)
            }
        } catch (e: IOException) {
            // Géocoding inverse impossible
        }
    }

    private fun updateMapMarker() {
        selectedLocation?.let { location ->
            googleMap?.clear()
            googleMap?.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Position de l'alarme")
            )
            updateRadiusCircle()
        }
    }

    private fun updateRadiusCircle() {
        selectedLocation?.let { location ->
            radiusCircle?.remove()

            val radius = binding.seekBarRadius.progress.toDouble()
            radiusCircle = googleMap?.addCircle(
                CircleOptions()
                    .center(location)
                    .radius(radius)
                    .strokeColor(0xFF0000FF.toInt())
                    .fillColor(0x220000FF)
                    .strokeWidth(2f)
            )
        }
    }

    private fun updateRadiusText() {
        val radius = binding.seekBarRadius.progress
        binding.textViewRadius.text = "Rayon: ${radius}m"
    }

    private fun saveAlarm() {
        val name = binding.editTextAlarmName.text.toString().trim()
        val location = selectedLocation

        if (name.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom pour l'alarme", Toast.LENGTH_SHORT).show()
            return
        }

        if (location == null) {
            Toast.makeText(this, "Veuillez sélectionner une position sur la carte", Toast.LENGTH_SHORT).show()
            return
        }

        val alarm = LocationAlarm(
            id = editingAlarm?.id ?: System.currentTimeMillis().toString(),
            name = name,
            latitude = location.latitude,
            longitude = location.longitude,
            radius = binding.seekBarRadius.progress.toFloat(),
            isActive = true,
            soundEnabled = binding.switchSound.isChecked,
            vibrationEnabled = binding.switchVibration.isChecked
        )

        alarmRepository.saveAlarm(alarm)
        Toast.makeText(this, "Alarme sauvegardée", Toast.LENGTH_SHORT).show()
        finish()
    }
}
