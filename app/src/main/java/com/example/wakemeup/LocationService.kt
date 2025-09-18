package com.example.wakemeup

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.AlarmClock
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var alarmRepository: AlarmRepository
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_service_channel"
        private const val ALARM_CHANNEL_ID = "alarm_channel"
        private const val LOCATION_UPDATE_INTERVAL = 10000L // 10 secondes
        private const val LOCATION_FASTEST_INTERVAL = 5000L // 5 secondes
    }

    override fun onCreate() {
        super.onCreate()

        alarmRepository = AlarmRepository(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(Vibrator::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        createNotificationChannels()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_ALARM") {
            stopAlarmSound()
            vibrator?.cancel()
            // Supprimer la notification d'alarme
            val alarmId = intent.getStringExtra("alarm_id")
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            alarmId?.let { notificationManager.cancel(it.hashCode()) }
            return START_STICKY
        }
        startForeground(NOTIFICATION_ID, createServiceNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Canal pour le service de localisation
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Service de localisation",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service de surveillance de la position GPS"
            }

            // Canal pour les alarmes
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Alarmes de réveil",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications d'alarme de réveil géographique"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    private fun createServiceNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WakeMeUp actif")
            .setContentText("Surveillance de votre position en cours...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    checkAlarms(location)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            // Les permissions ne sont pas accordées
        }
    }

    private fun checkAlarms(location: Location) {
        val activeAlarms = alarmRepository.getActiveAlarms()

        for (alarm in activeAlarms) {
            if (alarm.isInRange(location.latitude, location.longitude)) {
                triggerAlarm(alarm)
                // Désactiver l'alarme après qu'elle ait sonné
                val updatedAlarm = alarm.copy(isActive = false)
                alarmRepository.saveAlarm(updatedAlarm)
            }
        }
    }

    private fun triggerAlarm(alarm: LocationAlarm) {
        showAlarmNotification(alarm)

        // Choisir entre alarme système ou alarme personnalisée
        if (alarm.useSystemAlarm) {
            triggerSystemAlarm(alarm)
        } else {
            // Méthode personnalisée
            if (alarm.soundEnabled) {
                playAlarmSound()
            }

            if (alarm.vibrationEnabled) {
                startVibration()
            }
        }
    }

    /**
     * Déclenche l'alarme par défaut du système Android
     */
    private fun triggerSystemAlarm(alarm: LocationAlarm) {
        try {
            val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, "Réveil géographique: ${alarm.name}")
                putExtra(AlarmClock.EXTRA_HOUR, 0) // Heure immédiate
                putExtra(AlarmClock.EXTRA_MINUTES, 0)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true) // Déclenche immédiatement
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Vérifier si une app d'alarme est disponible
            if (alarmIntent.resolveActivity(packageManager) != null) {
                startActivity(alarmIntent)
            } else {
                // Fallback vers la méthode personnalisée
                playAlarmSound()
            }
        } catch (e: Exception) {
            // Fallback vers la méthode personnalisée en cas d'erreur
            playAlarmSound()
        }
    }

    /**
     * Alternative: Utiliser directement l'application d'alarme du système
     */
    private fun openSystemAlarmApp(alarm: LocationAlarm) {
        try {
            val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (alarmIntent.resolveActivity(packageManager) != null) {
                startActivity(alarmIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAlarmNotification(alarm: LocationAlarm) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent pour arrêter l'alarme
        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = "STOP_ALARM"
            putExtra("alarm_id", alarm.id)
        }
        val stopPendingIntent = PendingIntent.getService(
            this, alarm.id.hashCode(), stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setContentTitle("🚨 Réveil géographique!")
            .setContentText("Vous êtes arrivé près de: ${alarm.name}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Arrêter", stopPendingIntent)
            .build()

        notificationManager.notify(alarm.id.hashCode(), notification)
    }

    private fun playAlarmSound() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()

            // Arrêter le son après 30 secondes
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                stopAlarmSound()
            }, 30000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    private fun startVibration() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                val effect = VibrationEffect.createWaveform(pattern, 0)
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                @Suppress("DEPRECATION")
                it.vibrate(pattern, 0)
            }

            // Arrêter la vibration après 10 secondes
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                it.cancel()
            }, 10000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopAlarmSound()
        vibrator?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
