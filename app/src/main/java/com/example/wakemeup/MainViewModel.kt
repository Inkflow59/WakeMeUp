package com.example.wakemeup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmRepository = AlarmRepository(application)

    private val _alarms = MutableLiveData<List<LocationAlarm>>()
    val alarms: LiveData<List<LocationAlarm>> = _alarms

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            val alarmsList = withContext(Dispatchers.IO) {
                alarmRepository.getAllAlarms()
            }
            _alarms.value = alarmsList
        }
    }

    fun addAlarm(alarm: LocationAlarm) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                alarmRepository.saveAlarm(alarm)
            }
            loadAlarms()
        }
    }

    fun updateAlarm(alarm: LocationAlarm) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                alarmRepository.saveAlarm(alarm)
            }
            loadAlarms()
        }
    }

    fun deleteAlarm(alarm: LocationAlarm) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                alarmRepository.deleteAlarm(alarm.id)
            }
            loadAlarms()
        }
    }

    fun toggleAlarm(alarm: LocationAlarm) {
        val updatedAlarm = alarm.copy(isActive = !alarm.isActive)
        updateAlarm(updatedAlarm)
    }
}
