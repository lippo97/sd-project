package it.unibo.lpaas.domain

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.theory.Theory

fun Theory.getFactsByFunctor(functor: Functor): List<Fact> =
    clauses
        .asSequence()
//        .map{ println(it); it}
        .filter(Clause::isFact)
        .map{ println(it); it}
        .map { it.head }
        .filterNotNull()
        .filter { it.functor == functor.value }
        .map(Fact::of)
        .toList()
