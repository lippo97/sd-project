package it.unibo.lpaas.delivery

fun interface StringParser<T> {
    fun make(input: String): T

    operator fun invoke(input: String): T = make(input)
}
