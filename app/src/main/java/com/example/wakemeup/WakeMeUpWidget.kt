package com.example.wakemeup

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.widget.RemoteViews
import android.util.Log
import android.content.ComponentName

/**
 * Widget pour afficher les informations des alarmes GPS Wake Me Up
 */
class WakeMeUpWidget : AppWidgetProvider() {

    private val TAG = "WakeMeUpWidget"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "Widget activé")
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "Widget désactivé")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Mettre à jour le widget quand les alarmes changent
        if (intent.action == "com.example.wakemeup.ALARMS_UPDATED") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, WakeMeUpWidget::class.java)
            )
            onUpdate(context, appWidgetManager, widgetIds)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    try {
        // Récupérer les alarmes depuis le repository
        val alarmRepository = AlarmRepository(context)
        val allAlarms = alarmRepository.getAllAlarms()
        val activeAlarms = allAlarms.filter { it.isActive }

        // Construire la vue du widget
        val views = RemoteViews(context.packageName, R.layout.wake_me_up_widget)

        // Mettre à jour le nombre d'alarmes et le texte de statut
        when (activeAlarms.size) {
            0 -> {
                views.setTextViewText(R.id.widget_status_text, context.getString(R.string.widget_no_alarms))
                views.setTextViewText(R.id.widget_alarm_count, "0")
                views.setTextViewText(R.id.widget_next_alarm, "")
            }
            1 -> {
                views.setTextViewText(R.id.widget_status_text, context.getString(R.string.widget_one_alarm))
                views.setTextViewText(R.id.widget_alarm_count, "1")
                views.setTextViewText(R.id.widget_next_alarm, activeAlarms[0].name)
            }
            else -> {
                views.setTextViewText(
                    R.id.widget_status_text,
                    context.getString(R.string.widget_active_alarms, activeAlarms.size)
                )
                views.setTextViewText(R.id.widget_alarm_count, activeAlarms.size.toString())

                // Afficher la prochaine alarme (la plus récente créée)
                val nextAlarm = activeAlarms.maxByOrNull { it.createdAt }
                if (nextAlarm != null) {
                    views.setTextViewText(
                        R.id.widget_next_alarm,
                        context.getString(R.string.widget_next_alarm, nextAlarm.name)
                    )
                }
            }
        }

        // Configurer l'action du bouton pour ouvrir l'application
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_open_app_button, pendingIntent)

        // Ajouter une action de clic sur tout le widget pour ouvrir l'app
        views.setOnClickPendingIntent(R.id.widget_alarm_count, pendingIntent)

        // Mettre à jour le widget
        appWidgetManager.updateAppWidget(appWidgetId, views)

        Log.d("WakeMeUpWidget", "Widget mis à jour: ${activeAlarms.size} alarmes actives")

    } catch (e: Exception) {
        Log.e("WakeMeUpWidget", "Erreur lors de la mise à jour du widget", e)

        // Afficher une vue d'erreur minimale
        val views = RemoteViews(context.packageName, R.layout.wake_me_up_widget)
        views.setTextViewText(R.id.widget_status_text, "Erreur")
        views.setTextViewText(R.id.widget_alarm_count, "?")
        views.setTextViewText(R.id.widget_next_alarm, "")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}