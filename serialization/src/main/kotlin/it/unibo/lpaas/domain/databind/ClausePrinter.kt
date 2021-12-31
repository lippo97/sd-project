package it.unibo.lpaas.domain.databind

import it.unibo.tuprolog.core.Clause

object ClausePrinter {
    fun usingDefault(): Printer<Clause> = Printer {
        it.toString()
    }

    fun prettyPrinter(): Printer<Clause> = Printer {
        if (it.isFact) it.head.toString()
        else it.toString()
    }
}
