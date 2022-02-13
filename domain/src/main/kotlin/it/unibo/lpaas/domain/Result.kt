package it.unibo.lpaas.domain

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.Solution

sealed interface Result {

    val query: Struct

    data class Yes(
        override val query: Struct,
        val solvedQuery: Struct,
        val variables: Map<Variable, Term>
    ) : Result

    data class No(
        override val query: Struct,
    ) : Result

    data class Halt(
        override val query: Struct,
        val exception: Error
    ) : Result

    data class Error(
        val body: String?
    )

    companion object {
        fun of(result: Solution): Result =
            when (result) {
                is Solution.Yes -> Result.Yes(
                    result.query,
                    result.solvedQuery,
                    result.substitution.mapKeys { Variable(it.key.name) }
                )
                is Solution.No -> Result.No(result.query)
                is Solution.Halt -> Result.Halt(
                    result.query,
                    Error(result.exception.message)
                )
            }
    }
}
