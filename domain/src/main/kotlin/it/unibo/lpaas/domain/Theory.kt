package it.unibo.lpaas.domain

import it.unibo.tuprolog.core.Clause
import java.time.Instant
import it.unibo.tuprolog.theory.Theory as Theory2P

fun Theory2P.getFactsByFunctor(functor: Functor) =
    clauses
        .map { println(it); it }
        .filter(Clause::isFact)
        .map { it.head }
        .filter { it?.functor == functor.value }
        .map { Fact(it.toString()) }

data class Theory(
    val name: TheoryId,
    val data: Data,
    val version: Version,
    val createdAt: Instant = Instant.now(),
) {
    data class Data(val value: Theory2P)
}
