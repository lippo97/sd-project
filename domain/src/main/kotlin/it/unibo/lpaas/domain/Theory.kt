package it.unibo.lpaas.domain

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var
import java.time.Instant
import it.unibo.tuprolog.theory.Theory as Theory2P

data class Theory(
    val name: TheoryId,
    val data: Data,
    val version: Version,
    val createdAt: Instant = Instant.now(),
) {
    data class Data(val value: Theory2P) {
        fun assertZ(fact: Fact): Data = copy(
            value = value.assertZ(fact.struct)
        )

        fun assertA(fact: Fact): Data = copy(
            value = value.assertA(fact.struct)
        )

        fun retract(functor: Functor, arity: Int): Data {
            require(arity >= 0)
            val anonymousVars = (0 until arity).map { Var.anonymous() }
            val result = value.retractAll(Struct.of(functor.value, anonymousVars))
            return copy(value = result.theory)
        }
    }
}
