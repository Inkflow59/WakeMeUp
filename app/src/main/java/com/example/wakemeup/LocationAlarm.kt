package com.example.wakemeup

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationAlarm(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float, // Rayon en mètres
    val isActive: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {

    /**
     * Calcule la distance entre cette alarme et une position donnée
     */
    fun distanceTo(lat: Double, lng: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            latitude, longitude,
            lat, lng,
            results
        )
        return results[0]
    }

    /**
     * Vérifie si une position donnée est dans la zone de l'alarme
     */
    fun isInRange(lat: Double, lng: Double): Boolean {
        return distanceTo(lat, lng) <= radius
    }
}
