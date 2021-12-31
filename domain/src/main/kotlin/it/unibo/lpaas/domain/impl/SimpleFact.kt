package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Fact
import it.unibo.tuprolog.core.Fact as Fact2P

class SimpleFact(override val fact: Fact2P) : Fact {
    override fun equals(other: Any?): Boolean {
        if (other is Fact2P) {
            return fact == other
        }
        return false
    }

    override fun hashCode(): Int = fact.hashCode()
}
