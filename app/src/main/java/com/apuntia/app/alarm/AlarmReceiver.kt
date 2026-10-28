package com.apuntia.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/** Recibe el aviso del sistema cuando llega la hora de una alarma y arranca el servicio que la hace sonar. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmIntents.ACTION_FIRE) return
        val service = Intent(context, AlarmService::class.java)
            .setAction(AlarmIntents.ACTION_FIRE)
            .putExtras(intent)
        ContextCompat.startForegroundService(context, service)
    }
}
