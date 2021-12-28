package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId

class InMemoryGoalRepository(
    private var memory: Map<GoalId, Goal.Data> = mapOf()
) : GoalRepository by
BaseMemoryRepository(
    memory,
    "Goal",
    { id, value -> Goal(id, value) },
)
