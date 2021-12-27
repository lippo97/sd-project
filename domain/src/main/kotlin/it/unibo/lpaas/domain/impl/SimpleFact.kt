package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Fact
import it.unibo.tuprolog.core.Struct

class SimpleFact(override val struct: Struct) : Fact {
    override fun equals(other: Any?): Boolean {
        if (other is Fact) {
            return struct == other.struct
        }
        return false
    }

    override fun hashCode(): Int = struct.hashCode()
}
