package it.unibo.lpaas.delivery.http.handler.dto

import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId

data class CreateGoalDTO(val name: GoalId, val data: Goal.Data)

data class ReplaceGoalDTO(val data: Goal.Data)
