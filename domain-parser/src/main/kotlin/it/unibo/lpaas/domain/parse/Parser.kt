package it.unibo.lpaas.domain.parse

import it.unibo.lpaas.domain.Prolog
import kotlin.jvm.Throws

fun interface Parser<A, B> {
    @Throws(ParseException::class)
    fun parse(a: A): B

    companion object {
        val toProlog: Parser<String, Prolog> by lazy { RegexPrologParser() }
    }
}

fun Prolog.Companion.of(parser: Parser<String, Prolog>, input: String): Prolog = parser.parse(input)
