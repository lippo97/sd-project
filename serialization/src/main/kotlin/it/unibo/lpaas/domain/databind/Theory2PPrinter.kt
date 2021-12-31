package it.unibo.lpaas.domain.databind

import it.unibo.tuprolog.theory.Theory as Theory2P

object Theory2PPrinter {
    fun usingDefault(): Printer<Theory2P> = Printer {
        it.toString(true)
    }

    fun prettyPrinter(): Printer<Theory2P> = Printer { theory ->
        theory.clauses.map(ClausePrinter.prettyPrinter()::display)
            .fold("") { acc, value -> "$acc$value.\n" }
    }
}
