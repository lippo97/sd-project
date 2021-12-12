package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.GoalDataImpl
import it.unibo.lpaas.domain.impl.GoalImpl

interface GoalData {
    val subgoals: List<Subgoal>

    fun append(subGoal: Subgoal): GoalData

    fun replace(index: Int, subGoal: Subgoal): GoalData

    fun remove(index: Int): GoalData

    companion object {
        fun of(subgoals: List<Subgoal>): GoalData = GoalDataImpl(subgoals)
    }
}

interface Goal {
    val name: String

    val data: GoalData

    companion object {
        fun of(
            name: String,
            goalData: GoalData,
        ): Goal = GoalImpl(name, goalData)
    }
}
