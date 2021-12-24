package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.json

class MongoGoalRepository(
    private val goalCollection: CoroutineCollection<Goal>
) : GoalRepository {

    override suspend fun findAll(): List<Goal> =
        goalCollection.find().toList()

    override suspend fun findByName(name: GoalId): Goal {

        println((Goal::name eq name).json)
        return goalCollection.findOne(Goal::name eq name)
            ?: throw NotFoundException(name, "Goal")
    }

    override suspend fun create(name: GoalId, data: Goal.Data): Goal {
        val goalToCreate = Goal(name, data)
        goalCollection.insertOne(goalToCreate)
        return goalToCreate
    }

    override suspend fun updateByName(name: GoalId, data: Goal.Data): Goal =
        goalCollection.findOneAndReplace(Goal::name eq name, Goal(name, data))
            ?: throw (NotFoundException(name, "Goal"))

    override suspend fun deleteByName(name: GoalId): Goal =
        goalCollection.findOneAndDelete(Goal::name eq name)
            ?: throw (NotFoundException(name, "Goal"))
}
