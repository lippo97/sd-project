package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.SubGoal

class GoalImpl(
    override val name: String,
    override val subGoals: List<SubGoal>,
) : Goal
