package com.example.wakemeup

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AlarmRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAllAlarms(): List<LocationAlarm> {
        val json = prefs.getString("alarms", "[]")
        val type = object : TypeToken<List<LocationAlarm>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveAlarm(alarm: LocationAlarm) {
        val alarms = getAllAlarms().toMutableList()
        val existingIndex = alarms.indexOfFirst { it.id == alarm.id }

        if (existingIndex >= 0) {
            alarms[existingIndex] = alarm
        } else {
            alarms.add(alarm)
        }

        saveAlarms(alarms)
    }

    fun deleteAlarm(alarmId: String) {
        val alarms = getAllAlarms().toMutableList()
        alarms.removeAll { it.id == alarmId }
        saveAlarms(alarms)
    }

    private fun saveAlarms(alarms: List<LocationAlarm>) {
        val json = gson.toJson(alarms)
        prefs.edit().putString("alarms", json).apply()
    }

    fun getActiveAlarms(): List<LocationAlarm> {
        return getAllAlarms().filter { it.isActive }
    }
}
