package com.example.wakemeup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmRepository = AlarmRepository(application)

    private val _alarms = MutableLiveData<List<LocationAlarm>>()
    val alarms: LiveData<List<LocationAlarm>> = _alarms

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        _alarms.value = alarmRepository.getAllAlarms()
    }

    fun addAlarm(alarm: LocationAlarm) {
        alarmRepository.saveAlarm(alarm)
        loadAlarms()
    }

    fun updateAlarm(alarm: LocationAlarm) {
        alarmRepository.saveAlarm(alarm)
        loadAlarms()
    }

    fun deleteAlarm(alarm: LocationAlarm) {
        alarmRepository.deleteAlarm(alarm.id)
        loadAlarms()
    }

    fun toggleAlarm(alarm: LocationAlarm) {
        val updatedAlarm = alarm.copy(isActive = !alarm.isActive)
        updateAlarm(updatedAlarm)
    }
}
