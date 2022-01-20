package it.unibo.lpaas.core

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal

class GoalUseCases(private val goalRepository: GoalRepository) {

    companion object Tags {
        @JvmStatic
        @get:JvmName("getAllGoals")
        val getAllGoals = Tag("getAllGoals")

        @JvmStatic
        @get:JvmName("getAllGoalsIndex")
        val getAllGoalsIndex = Tag("getAllGoalsIndex")

        @JvmStatic
        @get:JvmName("getGoalByName")
        val getGoalByName = Tag("getGoalByName")

        @JvmStatic
        @get:JvmName("createGoal")
        val createGoal = Tag("createGoal")

        @JvmStatic
        @get:JvmName("replaceGoal")
        val replaceGoal = Tag("replaceGoal")

        @JvmStatic
        @get:JvmName("deleteGoal")
        val deleteGoal = Tag("deleteGoal")

        @JvmStatic
        @get:JvmName("appendSubgoal")
        val appendSubgoal = Tag("appendSubgoal")

        @JvmStatic
        @get:JvmName("getSubgoalByIndex")
        val getSubgoalByIndex = Tag("getSubgoalByIndex")

        @JvmStatic
        @get:JvmName("replaceSubgoal")
        val replaceSubgoal = Tag("replaceSubgoal")

        @JvmStatic
        @get:JvmName("deleteSubgoal")
        val deleteSubgoal = Tag("deleteSubgoal")
    }

    suspend fun getAllGoals(): List<Goal> =
        goalRepository.findAll()

    suspend fun getAllGoalsIndex(): List<GoalId> =
        goalRepository.findAll().map { it.name }

    suspend fun getGoalByName(name: GoalId): Goal =
        goalRepository.findByName(name)

    suspend fun createGoal(name: GoalId, data: Goal.Data): Goal =
        goalRepository.create(name, data)

    suspend fun replaceGoal(name: GoalId, data: Goal.Data): Goal =
        goalRepository.updateByName(name, data)

    suspend fun deleteGoal(name: GoalId): Goal =
        goalRepository.deleteByName(name)

    suspend fun appendSubgoal(name: GoalId, subGoal: Subgoal): Goal {
        val updatedData = goalRepository.findByName(name).data.append(subGoal)
        return goalRepository.updateByName(name, updatedData)
    }

    suspend fun getSubgoalByIndex(name: GoalId, index: Int): Subgoal =
        goalRepository.findByName(name).data.subgoals.run {
            if (index < size) this[index]
            else throw NotFoundException("$name/$index", "Goal")
        }

    suspend fun replaceSubgoal(name: GoalId, index: Int, subGoal: Subgoal): Goal {
        val updatedData = goalRepository.findByName(name).data.replace(index, subGoal)
        return goalRepository.updateByName(name, updatedData)
    }

    suspend fun deleteSubgoal(name: GoalId, index: Int): Goal {
        val updatedData = goalRepository.findByName(name).data.remove(index)
        return goalRepository.updateByName(name, updatedData)
    }
}
