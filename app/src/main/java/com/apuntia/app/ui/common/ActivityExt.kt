package com.apuntia.app.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Busca la actividad que contiene este contexto, o nulo si no hay. */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
