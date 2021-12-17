package it.unibo.lpaas.delivery.http.handler.dto

import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal

data class CreateGoalDTO(val name: GoalId, val subgoals: List<Subgoal>)

data class ReplaceGoalDTO(val subgoals: List<Subgoal>)
