package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalData

class InMemoryGoalRepository : GoalRepository {

    private var memory: Map<String, Goal> = mapOf()

    override suspend fun findAll(): List<Goal> =
        memory.values.toList()

    override suspend fun findByName(name: String): Goal =
        memory.getOrElse(name) { throw NotFoundException(name, "Goal") }

    override suspend fun create(name: String, goalData: GoalData): Goal {
        if (memory.containsKey(name)) throw DuplicateIdentifierException(name, "Goal")
        return Goal.of(name, goalData).apply {
            memory = memory + (name to this)
        }
    }

    override suspend fun updateByName(name: String, goalData: GoalData): Goal {
        if (!memory.containsKey(name)) throw NotFoundException(name, "Goal")
        val goal = Goal.of(name, goalData)
        memory.toMutableMap().apply {
            this[name] = goal
        }
        return goal
    }

    override suspend fun deleteByName(name: String): Goal {
        val removed = memory.toMutableMap().remove(name)
        return removed ?: throw NotFoundException(name, "Goal")
    }
}
