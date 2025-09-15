package com.example.wakemeup

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import android.util.Log

class AlarmRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
    private val TAG = "AlarmRepository"

    fun getAllAlarms(): List<LocationAlarm> {
        return try {
            val alarmIds = prefs.getStringSet("alarm_ids", emptySet()) ?: emptySet()
            alarmIds.mapNotNull { id ->
                loadAlarmById(id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du chargement des alarmes", e)
            emptyList()
        }
    }

    private fun loadAlarmById(id: String): LocationAlarm? {
        return try {
            val name = prefs.getString("alarm_${id}_name", null) ?: return null
            val latitude = prefs.getFloat("alarm_${id}_lat", 0f).toDouble()
            val longitude = prefs.getFloat("alarm_${id}_lng", 0f).toDouble()
            val radius = prefs.getFloat("alarm_${id}_radius", 100f)
            val isActive = prefs.getBoolean("alarm_${id}_active", false)
            val soundEnabled = prefs.getBoolean("alarm_${id}_sound", true)
            val vibrationEnabled = prefs.getBoolean("alarm_${id}_vibration", true)
            val createdAt = prefs.getLong("alarm_${id}_created", System.currentTimeMillis())

            LocationAlarm(
                id = id,
                name = name,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                isActive = isActive,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du chargement de l'alarme $id", e)
            null
        }
    }

    fun saveAlarm(alarm: LocationAlarm) {
        try {
            val alarmIds = prefs.getStringSet("alarm_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
            alarmIds.add(alarm.id)

            prefs.edit {
                putStringSet("alarm_ids", alarmIds)
                putString("alarm_${alarm.id}_name", alarm.name)
                putFloat("alarm_${alarm.id}_lat", alarm.latitude.toFloat())
                putFloat("alarm_${alarm.id}_lng", alarm.longitude.toFloat())
                putFloat("alarm_${alarm.id}_radius", alarm.radius)
                putBoolean("alarm_${alarm.id}_active", alarm.isActive)
                putBoolean("alarm_${alarm.id}_sound", alarm.soundEnabled)
                putBoolean("alarm_${alarm.id}_vibration", alarm.vibrationEnabled)
                putLong("alarm_${alarm.id}_created", alarm.createdAt)
            }
            Log.d(TAG, "Alarme sauvegardée avec succès: ${alarm.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la sauvegarde de l'alarme", e)
            throw e
        }
    }

    fun deleteAlarm(alarmId: String) {
        try {
            val alarmIds = prefs.getStringSet("alarm_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
            alarmIds.remove(alarmId)

            prefs.edit {
                putStringSet("alarm_ids", alarmIds)
                remove("alarm_${alarmId}_name")
                remove("alarm_${alarmId}_lat")
                remove("alarm_${alarmId}_lng")
                remove("alarm_${alarmId}_radius")
                remove("alarm_${alarmId}_active")
                remove("alarm_${alarmId}_sound")
                remove("alarm_${alarmId}_vibration")
                remove("alarm_${alarmId}_created")
            }
            Log.d(TAG, "Alarme supprimée avec succès: $alarmId")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de l'alarme", e)
            throw e
        }
    }

    fun getActiveAlarms(): List<LocationAlarm> {
        return try {
            getAllAlarms().filter { it.isActive }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du chargement des alarmes actives", e)
            emptyList()
        }
    }
}
