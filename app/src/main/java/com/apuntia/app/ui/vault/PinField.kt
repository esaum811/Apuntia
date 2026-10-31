package com.apuntia.app.ui.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apuntia.app.security.PinRules

// Solo deja escribir números del 0 al 9 y como máximo 8.
private val PinInput = InputTransformation {
    val text = asCharSequence()
    if (text.length > PinRules.MAX_LENGTH || !text.all { it in '0'..'9' }) revertAllChanges()
}

/**
 * Campo para escribir un PIN: solo números, hasta 8, y oculto en pantalla.
 *
 * @param label nombre visible del campo.
 * @param autoFocus verdadero para abrir el teclado al aparecer.
 */
@Composable
fun PinField(label: String, state: TextFieldState, autoFocus: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    if (autoFocus) LaunchedEffect(Unit) { focus.requestFocus() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        BasicSecureTextField(
            state = state,
            inputTransformation = PinInput,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = scheme.onBackground),
            cursorBrush = SolidColor(scheme.onBackground),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .background(scheme.surfaceVariant, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .focusRequester(focus)
                .semantics { contentDescription = label },
        )
    }
}

