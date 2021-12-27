package it.unibo.lpaas.domain

import it.unibo.tuprolog.core.Struct
import java.time.Instant
import it.unibo.tuprolog.theory.Theory as Theory2P

data class Theory(
    val name: TheoryId,
    val data: Data,
    val version: Version,
    val createdAt: Instant = Instant.now(),
) {
    data class Data(val value: Theory2P) {
        fun appendFact(fact: Fact) = copy(
            value = value.assertZ(
                Struct.of(fact.functor, fact.args.map { Struct.of(it) })
            )
        )

        fun prependFact(fact: Fact) = copy(
            value = value.assertA(
                Struct.of(fact.functor, fact.args.map { Struct.of(it) })
            )
        )
    }
}
