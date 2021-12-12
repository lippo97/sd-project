package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.GoalData
import it.unibo.lpaas.domain.Subgoal

internal class GoalDataImpl(override val subgoals: List<Subgoal>) : GoalData {
    override fun append(subGoal: Subgoal): GoalData =
        GoalDataImpl(subgoals + subGoal)

    override fun replace(index: Int, subGoal: Subgoal): GoalData = GoalDataImpl(
        subgoals.toMutableList().apply {
            this[index] = subGoal
        }
    )

    override fun remove(index: Int): GoalData = GoalDataImpl(
        subgoals.toMutableList().apply { removeFirst() }
    )
}
