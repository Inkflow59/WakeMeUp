package com.example.wakemeup

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.wakemeup.databinding.ActivityAlarmEditorBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.io.IOException
import java.util.*

class AlarmEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmEditorBinding
    private lateinit var alarmRepository: AlarmRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private var selectedLocation: GeoPoint? = null
    private var locationMarker: Marker? = null
    private var radiusCircle: Polygon? = null
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

        // Configuration osmdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

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
            selectedLocation = GeoPoint(alarm.latitude, alarm.longitude)
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
        try {
            mapView = binding.mapView

            // Configuration de la carte
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setMultiTouchControls(true)
            mapView.setBuiltInZoomControls(true)

            mapController = mapView.controller
            mapController.setZoom(15.0)

            // Position initiale
            selectedLocation?.let { location ->
                mapController.setCenter(location)
                updateMapMarker()
            } ?: run {
                // Position par défaut (Paris)
                val defaultLocation = GeoPoint(48.8566, 2.3522)
                mapController.setCenter(defaultLocation)
            }

            // Gestion des clics sur la carte
            mapView.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    val projection = mapView.projection
                    val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                    selectedLocation = geoPoint
                    updateMapMarker()
                    updateAddressFromLocation(geoPoint)
                }
                false
            }

            enableMyLocation()
        } catch (e: Exception) {
            android.util.Log.e("AlarmEditorActivity", "Erreur lors de l'initialisation de la carte", e)
            Toast.makeText(this, "Erreur lors du chargement de la carte OpenStreetMap", Toast.LENGTH_LONG).show()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // OSMDroid n'a pas de bouton "ma position" intégré, on peut l'ajouter manuellement si nécessaire
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                    selectedLocation = geoPoint
                    mapController.animateTo(geoPoint)
                    mapController.setZoom(15.0)
                    updateMapMarker()
                    updateAddressFromLocation(geoPoint)
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
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                selectedLocation = geoPoint
                mapController.animateTo(geoPoint)
                mapController.setZoom(15.0)
                updateMapMarker()
            } else {
                Toast.makeText(this, "Adresse non trouvée", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Erreur lors de la recherche d'adresse", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAddressFromLocation(geoPoint: GeoPoint) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)

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
            // Supprimer l'ancien marqueur
            locationMarker?.let { marker ->
                mapView.overlays.remove(marker)
            }

            // Créer un nouveau marqueur
            locationMarker = Marker(mapView).apply {
                position = location
                title = "Position de l'alarme"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            mapView.overlays.add(locationMarker)
            updateRadiusCircle()
            mapView.invalidate()
        }
    }

    private fun updateRadiusCircle() {
        selectedLocation?.let { location ->
            // Supprimer l'ancien cercle
            radiusCircle?.let { circle ->
                mapView.overlays.remove(circle)
            }

            // Créer un nouveau cercle de rayon
            val radiusInMeters = binding.seekBarRadius.progress.toDouble()
            radiusCircle = Polygon().apply {
                val points = arrayListOf<GeoPoint>()

                // Créer un cercle approximatif avec des points
                for (i in 0..360 step 10) {
                    val angle = Math.toRadians(i.toDouble())
                    val lat = location.latitude + (radiusInMeters / 111320.0) * Math.cos(angle)
                    val lng = location.longitude + (radiusInMeters / (111320.0 * Math.cos(Math.toRadians(location.latitude)))) * Math.sin(angle)
                    points.add(GeoPoint(lat, lng))
                }

                setPoints(points)
                fillColor = 0x220000FF
                strokeColor = 0xFF0000FF.toInt()
                strokeWidth = 2f
            }

            mapView.overlays.add(radiusCircle)
            mapView.invalidate()
        }
    }

    private fun updateRadiusText() {
        val radius = binding.seekBarRadius.progress
        binding.textViewRadius.text = "Rayon: ${radius}m"
    }

    private fun saveAlarm() {
        val name = binding.editTextAlarmName.text.toString().trim()
        var location = selectedLocation

        if (name.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom pour l'alarme", Toast.LENGTH_SHORT).show()
            return
        }

        // Si aucune position n'est sélectionnée, essayer de géocoder l'adresse saisie
        if (location == null) {
            val address = binding.editTextAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocationName(address, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val foundLocation = addresses[0]
                        location = GeoPoint(foundLocation.latitude, foundLocation.longitude)
                        selectedLocation = location
                      }
                } catch (e: IOException) {
                    android.util.Log.e("AlarmEditorActivity", "Erreur de géocodage", e)
                }
            }
        }

        if (location == null) {
            Toast.makeText(this, "Veuillez sélectionner une position sur la carte ou entrer une adresse", Toast.LENGTH_SHORT).show()
            return
        }

        try {
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
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors de la sauvegarde: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("AlarmEditorActivity", "Erreur lors de la sauvegarde", e)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
