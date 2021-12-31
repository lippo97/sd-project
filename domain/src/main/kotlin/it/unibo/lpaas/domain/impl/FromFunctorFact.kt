package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Argument
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Fact as Fact2P

class FromFunctorFact(override val functor: Functor, arguments: List<Argument>) : Fact {
    override val fact: Fact2P =
        Fact2P.of(Struct.of(functor.value, arguments.map { Struct.of(it.value) }))

    override val arity: Int = arguments.size

    override fun equals(other: Any?): Boolean {
        if (other is Fact) {
            return fact == other.fact
        }
        return false
    }

    override fun hashCode(): Int = fact.hashCode()
}
