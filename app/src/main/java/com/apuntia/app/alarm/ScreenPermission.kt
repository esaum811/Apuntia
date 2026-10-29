package com.apuntia.app.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService

/** Permisos que hacen que la pantalla de alarma aparezca sola, aunque el teléfono esté bloqueado o en uso. */
enum class ScreenPermission {
    /** "Mostrar sobre otras apps": deja abrir la pantalla de alarma mientras se usa otra app. */
    OVERLAY,

    /** "Notificaciones en pantalla completa": deja abrir la pantalla de alarma con el teléfono bloqueado. */
    FULL_SCREEN,
}

/** Devuelve el primer permiso de pantalla que falta, o nulo si están todos concedidos. */
fun missingScreenPermission(context: Context): ScreenPermission? = when {
    !Settings.canDrawOverlays(context) -> ScreenPermission.OVERLAY
    !canUseFullScreenIntent(context) -> ScreenPermission.FULL_SCREEN
    else -> null
}

private fun canUseFullScreenIntent(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
        context.getSystemService<NotificationManager>()?.canUseFullScreenIntent() != false

/** Pantalla de ajustes del sistema donde se concede [permission] a Apuntia. */
fun screenPermissionSettings(context: Context, permission: ScreenPermission): Intent {
    val uri = Uri.parse("package:${context.packageName}")
    return when {
        permission == ScreenPermission.OVERLAY -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
            Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, uri)
        else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
    }
}
