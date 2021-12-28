package it.unibo.lpaas.persistence.ext

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.persistence.InMemoryGoalRepository

fun GoalRepository.inMemory(
    memory: Map<GoalId, Goal.Data> = mapOf(),
): GoalRepository =
    InMemoryGoalRepository(memory)
