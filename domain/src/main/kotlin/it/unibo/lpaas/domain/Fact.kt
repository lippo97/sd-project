package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.FromFunctorFact
import it.unibo.lpaas.domain.impl.SimpleFact
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct

/**
 * A Prolog fact that can hold only compound terms and simple atoms.
 */
interface Fact {
    val struct: Struct

    val functor: Functor
        get() = Functor(struct.functor)

    val arity: Int
        get() = struct.arity

    companion object {
        fun of(functor: Functor, vararg args: String): Fact =
            FromFunctorFact(functor, args.map { Argument(it) }.toList())

        @Throws(IllegalArgumentException::class)
        fun of(clause: Clause): Fact {
            val head = clause.head
            require(clause.isFact && head != null) { "The provided Struct is not a fact." }
            return SimpleFact(head)
        }

        fun of(struct: Struct): Fact {
            return SimpleFact(struct)
        }
    }
}
