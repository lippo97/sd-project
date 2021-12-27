package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Argument
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
import it.unibo.tuprolog.core.Struct

class FromFunctorFact(override val functor: Functor, arguments: List<Argument>) : Fact {
    override val struct: Struct =
        Struct.of(functor.value, arguments.map { Struct.of(it.value) })

    override val arity: Int = arguments.size

    override fun equals(other: Any?): Boolean {
        if (other is Fact) {
            return struct == other.struct
        }
        return false
    }

    override fun hashCode(): Int = struct.hashCode()
}
