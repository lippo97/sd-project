package it.unibo.lpaas.client.repl

import it.unibo.lpaas.domain.Result
import it.unibo.tuprolog.core.parsing.ParseException

fun interface Formatter<A> {
    fun format(a: A): String

    companion object {
        val parseException: Formatter<ParseException> = Formatter { "# ${it.message}" }

        val result: Formatter<Result> = Formatter {
            when (it) {
                is Result.Yes ->
                    "yes: ${it.solvedQuery}" +
                        it.variables
                            .map { (v, k) -> "    ${v.value} = $k" }
                            .fold("") { acc, v -> "$acc\n$v" }
                is Result.No -> "no."
                is Result.Halt -> "halt: ${it.exception.body}"
            }
        }
    }
}
