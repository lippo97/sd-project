package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalData

internal class GoalImpl(
    override val name: String,
    override val data: GoalData,
) : Goal
