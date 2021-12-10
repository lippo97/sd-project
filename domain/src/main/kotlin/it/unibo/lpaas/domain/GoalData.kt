package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.GoalImpl
import it.unibo.lpaas.domain.impl.SubGoalImpl

interface GoalData {
    val subGoals: List<SubGoal>
}

interface Goal : GoalData {
    val name: String

    companion object {
        fun of(
            name: String,
            subGoals: List<SubGoal>,
        ): Goal = GoalImpl(name, subGoals)

        fun of(
            name: String,
            term: Term,
        ): Goal = GoalImpl(name, listOf(SubGoalImpl(term)))
    }
}
