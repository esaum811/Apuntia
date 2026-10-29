package com.apuntia.app.alarm

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.addCallback
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.apuntia.app.ApuntiaApplication
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Reminder
import com.apuntia.app.ui.alarms.RingingScreen
import com.apuntia.app.ui.theme.ApuntiaTheme
import kotlinx.coroutines.flow.first

/**
 * Pantalla completa que se abre cuando suena una alarma, incluso con el teléfono bloqueado.
 * Muestra la hora y la etiqueta, con los botones Terminar y Posponer.
 */
class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showOverLockScreen()
        val id = intent.getStringExtra(AlarmIntents.EXTRA_REMINDER_ID)
        if (id == null) {
            finish()
            return
        }
        // La pantalla solo se cierra con Terminar o Posponer; el botón Atrás no hace nada.
        onBackPressedDispatcher.addCallback(this) {}
        val container = (applicationContext as ApuntiaApplication).container
        setContent {
            // Si la alarma termina por otro camino (botón de la notificación o tiempo agotado), se cierra sola.
            LaunchedEffect(Unit) { AlarmState.isRinging.collect { ringing -> if (!ringing) finish() } }
            ApuntiaTheme {
                val data by produceState<Pair<Reminder, Category?>?>(null, id) {
                    val reminder = container.reminders.get(id)
                    val categories = container.categories.observeAll().first()
                    val category = categories.firstOrNull { it.id == reminder?.categoryId }
                    value = reminder?.let { it to category }
                }
                data?.let { (reminder, category) ->
                    RingingScreen(
                        reminder = reminder,
                        category = category,
                        onDismiss = {
                            startService(alarmServiceCommand(this, id, AlarmIntents.ACTION_DISMISS))
                            finish()
                        },
                        onSnooze = {
                            startService(alarmServiceCommand(this, id, AlarmIntents.ACTION_SNOOZE))
                            finish()
                        },
                    )
                }
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
