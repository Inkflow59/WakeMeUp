package com.example.wakemeup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf<String>().apply {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Vérifie si toutes les permissions nécessaires sont accordées
     */
    fun areAllPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Retourne la liste des permissions manquantes
     */
    fun getMissingPermissions(): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Vérifie si une permission spécifique a été refusée de manière permanente
     */
    fun isPermissionPermanentlyDenied(permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !activity.shouldShowRequestPermissionRationale(permission) &&
                    ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        } else {
            // Pour les versions antérieures à Android 6.0, les permissions sont accordées à l'installation
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Affiche un dialogue explicatif et redirige vers les paramètres de l'application
     */
    fun showPermissionSettingsDialog() {
        val missingPermissions = getMissingPermissions()

        if (missingPermissions.isEmpty()) return

        val permissionNames = missingPermissions.joinToString("\n") { permission ->
            when (permission) {
                Manifest.permission.ACCESS_FINE_LOCATION -> "• Localisation précise"
                Manifest.permission.ACCESS_COARSE_LOCATION -> "• Localisation approximative"
                Manifest.permission.ACCESS_BACKGROUND_LOCATION -> "• Localisation en arrière-plan"
                Manifest.permission.POST_NOTIFICATIONS -> "• Notifications"
                else -> "• $permission"
            }
        }

        AlertDialog.Builder(activity)
            .setTitle("Permissions requises")
            .setMessage(
                "WakeMeUp a besoin des permissions suivantes pour fonctionner correctement :\n\n" +
                "$permissionNames\n\n" +
                "Veuillez les activer dans les paramètres de l'application."
            )
            .setPositiveButton("Ouvrir les paramètres") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Quitter") { _, _ ->
                activity.finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Affiche un dialogue d'explication avant de demander les permissions
     */
    fun showPermissionRationale(onContinue: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Permissions nécessaires")
            .setMessage(
                "WakeMeUp a besoin d'accéder à votre localisation pour :\n\n" +
                "• Détecter quand vous approchez de votre destination\n" +
                "• Fonctionner en arrière-plan pendant vos trajets\n" +
                "• Vous envoyer des notifications d'alarme\n\n" +
                "Ces permissions sont essentielles pour le bon fonctionnement de l'application."
            )
            .setPositiveButton("Continuer") { _, _ ->
                onContinue()
            }
            .setNegativeButton("Annuler") { _, _ ->
                activity.finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Ouvre les paramètres de l'application
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Fallback vers les paramètres généraux si l'intent spécifique échoue
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
            activity.startActivity(fallbackIntent)
        }
    }

    /**
     * Vérifie les permissions d'optimisation de batterie (optionnel mais recommandé)
     */
    fun checkBatteryOptimization(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = activity.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isIgnoringBatteryOptimizations(activity.packageName)
        } else {
            true
        }
    }

    /**
     * Affiche un dialogue pour désactiver l'optimisation de batterie
     */
    fun showBatteryOptimizationDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder(activity)
                .setTitle("Optimisation de la batterie")
                .setMessage(
                    "Pour un fonctionnement optimal en arrière-plan, il est recommandé de désactiver " +
                    "l'optimisation de la batterie pour WakeMeUp.\n\n" +
                    "Cela permettra à l'application de continuer à surveiller votre position " +
                    "même quand l'écran est éteint."
                )
                .setPositiveButton("Paramètres") { _, _ ->
                    openBatteryOptimizationSettings()
                }
                .setNegativeButton("Plus tard") { _, _ -> }
                .show()
        }
    }

    /**
     * Ouvre les paramètres d'optimisation de la batterie
     */
    private fun openBatteryOptimizationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            try {
                activity.startActivity(intent)
            } catch (e: Exception) {
                // Fallback
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                activity.startActivity(fallbackIntent)
            }
        }
    }
}
