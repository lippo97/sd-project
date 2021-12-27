package it.unibo.lpaas.domain

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.theory.Theory

fun Theory.getFactsByFunctor(functor: Functor): List<Fact> =
    clauses
        .asSequence()
        .filter(Clause::isFact)
        .map { it.head }
        .filterNotNull()
        .filter { it.functor == functor.value }
        .map {
            println(it)
            println(it.functor)
            println(it.args)
            it
        }
        .map(Fact::of)
        .toList()
