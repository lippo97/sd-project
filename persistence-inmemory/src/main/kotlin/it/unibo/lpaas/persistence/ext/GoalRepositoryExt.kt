package it.unibo.lpaas.persistence.ext

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.persistence.InMemoryGoalRepository

fun GoalRepository.Companion.inMemory(
    memory: Map<GoalId, Goal.Data> = mapOf(),
): GoalRepository =
    InMemoryGoalRepository(memory)

fun GoalRepository.Companion.inMemory(vararg tuples: Pair<GoalId, Goal.Data>): GoalRepository =
    inMemory(tuples.toMap())
