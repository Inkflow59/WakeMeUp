package com.example.wakemeup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Redémarrer le service de géolocalisation après le reboot
            val alarmRepository = AlarmRepository(context)
            val activeAlarms = alarmRepository.getActiveAlarms()

            // Ne démarrer le service que s'il y a des alarmes actives
            if (activeAlarms.isNotEmpty()) {
                val serviceIntent = Intent(context, LocationService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
