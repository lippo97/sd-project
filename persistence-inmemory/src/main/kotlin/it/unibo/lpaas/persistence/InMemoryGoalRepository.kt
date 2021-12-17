package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId

class InMemoryGoalRepository(
    private var memory: Map<GoalId, Goal.Data> = mapOf()
) : GoalRepository {

    override suspend fun findAll(): List<Goal> =
        memory.entries.map { (name, data) -> Goal(name, data) }

    override suspend fun findByName(name: GoalId): Goal = Goal(
        name,
        memory.getOrElse(name) { throw NotFoundException(name, "Goal") }
    )

    override suspend fun create(name: GoalId, data: Goal.Data): Goal {
        if (memory.containsKey(name)) throw DuplicateIdentifierException(name, "Goal")
        memory = memory + (name to data)
        return Goal(name, data)
    }

    override suspend fun updateByName(name: GoalId, data: Goal.Data): Goal {
        if (!memory.containsKey(name)) throw NotFoundException(name, "Goal")
        memory = memory.toMutableMap().apply {
            this[name] = data
        }
        return Goal(name, data)
    }

    override suspend fun deleteByName(name: GoalId): Goal {
        return findByName(name).also {
            memory = memory - name
        }
    }
}
