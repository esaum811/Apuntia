package com.apuntia.app.ui.settings

import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService
import com.apuntia.app.alarm.ScreenPermission
import com.apuntia.app.alarm.screenPermissionSettings

/** Verdadero si [permission] ya está concedido. */
internal fun isGranted(context: Context, permission: ScreenPermission): Boolean = when (permission) {
    ScreenPermission.OVERLAY -> Settings.canDrawOverlays(context)
    ScreenPermission.FULL_SCREEN ->
        Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            context.getSystemService<NotificationManager>()?.canUseFullScreenIntent() != false
}

/** Verdadero si los dos permisos de la pantalla de alarma están concedidos. */
internal fun alarmPermissionsGranted(context: Context): Boolean =
    ScreenPermission.entries.all { isGranted(context, it) }

/** Abre la pantalla del sistema donde se concede [permission]; si no existe, abre los datos de la app. */
internal fun openAlarmPermission(context: Context, permission: ScreenPermission) {
    try {
        context.startActivity(screenPermissionSettings(context, permission))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")),
            )
        } catch (_: ActivityNotFoundException) {
            // Sin pantalla de ajustes de la app no hay nada que mostrar.
        }
    }
}
