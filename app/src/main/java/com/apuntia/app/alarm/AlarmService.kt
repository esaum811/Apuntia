package com.apuntia.app.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import com.apuntia.app.ApuntiaApplication
import com.apuntia.app.R
import com.apuntia.app.domain.model.RepeatRule
import com.apuntia.app.domain.model.Reminder
import com.apuntia.app.domain.usecase.NextAlarmTime
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "Apuntia"
private const val CHANNEL_ID = "apuntia_alarms"
private const val NOTIFICATION_ID = 1001
private const val SNOOZE_MINUTES = 10L
private const val MILLIS_PER_MINUTE = 60_000L
private const val MAX_RING_MINUTES = 10L
private val VIBRATION_PATTERN = longArrayOf(0, 600, 600)

/**
 * Hace sonar la alarma: reproduce el sonido en bucle, vibra y muestra una notificación que abre
 * la pantalla de alarma. Se detiene al terminar, al posponer o a los 10 minutos.
 */
class AlarmService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var player: MediaPlayer? = null
    private var timeout: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(AlarmIntents.EXTRA_REMINDER_ID)
        when {
            intent == null || id == null -> stopSelf()
            intent.action == AlarmIntents.ACTION_DISMISS -> stopRinging()
            intent.action == AlarmIntents.ACTION_SNOOZE -> snooze(id)
            else -> startRinging(id, intent.getBooleanExtra(AlarmIntents.EXTRA_IS_SNOOZE, false))
        }
        return START_NOT_STICKY
    }

    private fun startRinging(id: String, isSnooze: Boolean) {
        // Primero se marca que suena: la pantalla de alarma se cierra sola si ve que no hay ninguna sonando.
        AlarmState.setRinging(true)
        // Android exige mostrar la notificación enseguida; el texto real llega cuando se lee la alarma.
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(id, getString(R.string.alarm_default_label)),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )
        showAlarmScreen(id)
        scope.launch {
            @Suppress("TooGenericExceptionCaught")
            val reminder = try {
                loadAndAdvance(id, isSnooze)
            } catch (error: Exception) {
                Log.e(TAG, "No se pudo leer la alarma", error)
                null
            }
            // Aunque falle la base de datos, la alarma debe sonar.
            reminder?.let {
                postNotification(buildNotification(id, it.label.ifBlank { getString(R.string.alarm_default_label) }))
            }
            playSound(reminder?.ringtoneUri)
            vibrate()
        }
        timeout?.cancel()
        timeout = scope.launch {
            delay(MAX_RING_MINUTES * MILLIS_PER_MINUTE)
            stopRinging()
        }
    }

    // Abre la pantalla de alarma encima de lo que haya. Con el teléfono bloqueado lo hace la notificación
    // de pantalla completa; con otra app abierta, el permiso "Mostrar sobre otras apps".
    private fun showAlarmScreen(id: String) {
        val screen = Intent(this, AlarmActivity::class.java)
            .putExtra(AlarmIntents.EXTRA_REMINDER_ID, id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(screen)
    }

    // Deja la programación lista para la próxima vez: se repite o se desactiva. Una posposición no la toca.
    private suspend fun loadAndAdvance(id: String, isSnooze: Boolean): Reminder? {
        val container = (applicationContext as ApuntiaApplication).container
        val reminder = container.reminders.get(id)
        if (reminder != null && !isSnooze) {
            if (reminder.repeatRule == RepeatRule.NONE) {
                container.reminders.setEnabled(id, false)
            } else {
                val now = System.currentTimeMillis()
                val next = NextAlarmTime()(reminder.triggerAt, reminder.repeatRule, now, ZoneId.systemDefault())
                val updated = reminder.copy(triggerAt = next)
                container.reminders.save(updated)
                container.alarmScheduler.schedule(updated)
            }
        }
        return reminder
    }

    private fun snooze(id: String) {
        val container = (applicationContext as ApuntiaApplication).container
        container.alarmScheduler.scheduleSnooze(id, System.currentTimeMillis() + SNOOZE_MINUTES * MILLIS_PER_MINUTE)
        stopRinging()
    }

    private fun playSound(ringtoneUri: String?) {
        val uri = ringtoneUri?.let(Uri::parse) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        // Si ya sonaba otra alarma, se libera su reproductor antes de empezar el nuevo.
        player?.release()
        @Suppress("TooGenericExceptionCaught")
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (error: Exception) {
            Log.e(TAG, "No se pudo reproducir el sonido de la alarma", error)
        }
    }

    private fun vibrate() {
        getSystemService<Vibrator>()?.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0))
    }

    private fun stopRinging() {
        AlarmState.setRinging(false)
        timeout?.cancel()
        player?.run {
            runCatching { stop() }
            release()
        }
        player = null
        getSystemService<Vibrator>()?.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        AlarmState.setRinging(false)
        scope.cancel()
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun postNotification(notification: Notification) {
        getSystemService<NotificationManager>()?.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(id: String, label: String): Notification {
        createChannel()
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, AlarmActivity::class.java).putExtra(AlarmIntents.EXTRA_REMINDER_ID, id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText(getString(R.string.alarm_ringing))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(open)
            .setFullScreenIntent(open, true)
            .addAction(0, getString(R.string.alarm_snooze_short), serviceAction(id, AlarmIntents.ACTION_SNOOZE))
            .addAction(0, getString(R.string.alarm_dismiss), serviceAction(id, AlarmIntents.ACTION_DISMISS))
            .build()
    }

    private fun serviceAction(id: String, action: String): PendingIntent = PendingIntent.getService(
        this,
        action.hashCode(),
        alarmServiceCommand(this, id, action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun createChannel() {
        val name = getString(R.string.alarm_channel_name)
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
        // El sonido lo reproduce el servicio, no la notificación.
        channel.setSound(null, null)
        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }
}

/** Crea la orden para terminar o posponer la alarma [id] desde otra pantalla. */
internal fun alarmServiceCommand(context: Context, id: String, action: String): Intent =
    Intent(context, AlarmService::class.java).setAction(action).putExtra(AlarmIntents.EXTRA_REMINDER_ID, id)
