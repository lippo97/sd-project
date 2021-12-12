package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.SubgoalImpl

interface Subgoal {
    val value: Term

    companion object {
        fun of(term: Term): Subgoal = SubgoalImpl(term)
    }
}
