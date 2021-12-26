package it.unibo.lpaas.delivery

fun interface StringParser<T> {
    fun parse(input: String): T
}
