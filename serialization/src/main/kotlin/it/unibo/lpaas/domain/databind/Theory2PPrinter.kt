package it.unibo.lpaas.domain.databind

import it.unibo.tuprolog.theory.Theory as Theory2P

fun interface Theory2PPrinter {
    fun display(theory2P: Theory2P): String

    companion object {
        fun usingDefault(): Theory2PPrinter = Theory2PPrinter {
            it.toString(true)
        }

        fun prettyPrinter(): Theory2PPrinter = Theory2PPrinter { theory ->
            theory.clauses.map {
                if (it.isFact) it.head.toString()
                else it.toString()
            }
                .fold("") { acc, value -> "$acc$value.\n" }
        }
    }
}
