package com.apuntia.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apuntia.app.ApuntiaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "Apuntia"

/** Al encender el teléfono o actualizar la app, vuelve a programar las alarmas activas. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val container = (context.applicationContext as ApuntiaApplication).container
        // goAsync deja que el trabajo termine aunque onReceive ya haya regresado.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            @Suppress("TooGenericExceptionCaught")
            try {
                AlarmRescheduler(container.reminders, container.alarmScheduler).rescheduleAll()
            } catch (error: Exception) {
                Log.e(TAG, "No se pudieron reprogramar las alarmas", error)
            } finally {
                pending.finish()
            }
        }
    }
}
