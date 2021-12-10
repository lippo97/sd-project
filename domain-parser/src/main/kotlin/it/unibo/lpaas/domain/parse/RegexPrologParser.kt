package it.unibo.lpaas.domain.parse

import it.unibo.lpaas.domain.Prolog

private data class PrologImpl(override val value: String) : Prolog

internal class RegexPrologParser : Parser<String, Prolog> {
    override fun parse(a: String): Prolog =
}
