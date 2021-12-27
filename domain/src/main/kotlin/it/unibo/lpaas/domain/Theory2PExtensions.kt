package it.unibo.lpaas.domain

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.theory.Theory

fun Theory.getFactsByFunctor(functor: Functor): List<Fact> =
    clauses
        .filter(Clause::isFact)
        .map { it.head }
        .filter { it?.functor == functor.value }
        .map { Fact(it!!.functor, it.args.map { it.toString() }) }
