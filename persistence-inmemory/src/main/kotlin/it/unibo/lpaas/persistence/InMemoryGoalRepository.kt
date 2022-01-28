package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId

class InMemoryGoalRepository(
    memory: Map<GoalId, Goal.Data> = mapOf()
) : GoalRepository by
GoalRepository.of(
    BaseMemoryRepository(
        memory,
        "Goal",
        { id, value -> Goal(id, value) },
    )
)

fun GoalRepository.Companion.inMemory(
    memory: Map<GoalId, Goal.Data> = mapOf(),
): GoalRepository =
    InMemoryGoalRepository(memory)

fun GoalRepository.Companion.inMemory(vararg tuples: Pair<GoalId, Goal.Data>): GoalRepository =
    inMemory(tuples.toMap())
