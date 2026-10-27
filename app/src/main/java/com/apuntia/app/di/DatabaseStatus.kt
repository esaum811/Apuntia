package com.apuntia.app.di

/** Estado de la base de datos al abrir la app. */
enum class DatabaseStatus {
    /** Todavía se está abriendo. */
    LOADING,

    /** Lista para usarse. */
    READY,

    /** No se pudo abrir; se puede reintentar. */
    FAILED,
}
