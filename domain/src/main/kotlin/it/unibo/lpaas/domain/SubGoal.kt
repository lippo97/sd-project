package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.SubGoalImpl

interface SubGoal {
    val value: Term

    companion object {
        fun of(term: Term): SubGoal = SubGoalImpl(term)
    }
}
