package it.unibo.lpaas.domain.databind

fun interface Printer<T> {
    fun display(t: T): String
}
