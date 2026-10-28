package com.apuntia.app.ui.alarms

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apuntia.app.R
import com.apuntia.app.domain.model.Category
import com.apuntia.app.domain.model.Reminder
import com.apuntia.app.ui.common.displayTime
import com.apuntia.app.ui.components.CategoryShapeIcon
import com.apuntia.app.ui.theme.ApuntiaTheme
import com.apuntia.app.ui.theme.alarmTimeStyle
import com.apuntia.app.ui.theme.cardInk

private const val PULSE_MILLIS = 1_400
private const val PULSE_END_SCALE = 1.6f
private val RingSize = 64.dp

/**
 * Pantalla completa que se muestra cuando suena la alarma.
 *
 * @param category categoría de la alarma, o nulo si ya no existe.
 * @param onDismiss se llama al tocar Terminar.
 * @param onSnooze se llama al tocar Posponer.
 */
@Composable
fun RingingScreen(reminder: Reminder, category: Category?, onDismiss: () -> Unit, onSnooze: () -> Unit) {
    val colors = ApuntiaTheme.colors
    val time = displayTime(LocalContext.current, reminder.triggerAt)
    val cardColor = category?.let { Color(it.color) } ?: colors.accent
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().weight(1f).background(cardColor, RoundedCornerShape(32.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PulsingBell()
            Text(
                stringResource(R.string.alarm_ringing_now),
                style = MaterialTheme.typography.labelLarge,
                color = cardInk,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Hora más pequeña que alarmTimeStyle (168 sp) para que quepa en pantallas angostas.
                Text(
                    time.digits,
                    style = alarmTimeStyle.copy(fontSize = 112.sp, lineHeight = 96.sp),
                    color = cardInk,
                    maxLines = 1,
                    softWrap = false,
                )
                time.period?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(bottom = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = cardInk,
                    )
                }
            }
            Text(
                reminder.label.ifBlank { stringResource(R.string.alarm_default_label) },
                style = MaterialTheme.typography.headlineMedium,
                color = cardInk,
                textAlign = TextAlign.Center,
            )
            category?.let {
                Row(
                    Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CategoryShapeIcon(it.shape, cardInk)
                    Text(it.name, style = MaterialTheme.typography.labelMedium, color = cardInk)
                }
            }
        }
        BigButton(stringResource(R.string.alarm_dismiss), colors.accent, colors.onAccent, onDismiss)
        BigButton(
            stringResource(R.string.alarm_snooze_long),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onBackground,
            onSnooze,
        )
    }
}

@Composable
private fun BigButton(text: String, background: Color, content: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .background(background, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

// Círculos que se expanden. Si el usuario desactivó las animaciones en Android, se quedan quietos.
@Composable
private fun PulsingBell() {
    val resolver = LocalContext.current.contentResolver
    val animate = remember { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f }
    val progress by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f,
        targetValue = if (animate) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseProgress",
    )
    Box(Modifier.padding(bottom = 16.dp).size(RingSize * PULSE_END_SCALE), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(RingSize)
                .scale(1f + (PULSE_END_SCALE - 1f) * progress)
                .alpha(1f - progress)
                .border(3.dp, cardInk, CircleShape),
        )
        Box(Modifier.size(RingSize / 2).background(cardInk, CircleShape))
    }
}
