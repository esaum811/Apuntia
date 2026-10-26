package com.apuntia.app.core

import java.util.UUID

/** Crea identificadores únicos para categorías, entradas y alarmas. */
object IdGenerator {
    /** Devuelve un identificador nuevo (UUID en texto). */
    fun newId(): String = UUID.randomUUID().toString()
}
