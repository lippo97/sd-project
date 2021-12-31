package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.FromFunctorFact
import it.unibo.lpaas.domain.impl.SimpleFact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Fact as Fact2P

/**
 * A Prolog fact that can hold only compound terms and simple atoms.
 */
interface Fact {
    val fact: Fact2P

    val functor: Functor
        get() = Functor(fact.head.functor)

    val arity: Int
        get() = fact.head.arity

    companion object {
        fun of(functor: Functor, vararg args: String): Fact =
            FromFunctorFact(functor, args.map { Argument(it) }.toList())

        @Throws(IllegalArgumentException::class)
        fun of(fact2p: Fact2P): Fact {
//            val head = clause.head
//            require(clause.isFact && head != null) { "The provided Struct is not a fact." }
            return SimpleFact(fact2p)
        }

        fun of(struct: Struct): Fact {
            return SimpleFact(Fact2P.of(struct))
        }
    }
}
