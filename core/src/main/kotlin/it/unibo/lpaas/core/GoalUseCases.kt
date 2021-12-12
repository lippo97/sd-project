package it.unibo.lpaas.core

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.repository.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalData
import it.unibo.lpaas.domain.Subgoal

class GoalUseCases(private val goalRepository: GoalRepository) {
    suspend fun getAllGoals(): List<Goal> = goalRepository.findAll()

    @Throws(NotFoundException::class)
    suspend fun getGoalByName(name: String): Goal = goalRepository.findByName(name)

    suspend fun createGoal(name: String, goalData: GoalData): Goal = goalRepository.create(name, goalData)

    @Throws(NotFoundException::class)
    suspend fun replaceGoal(name: String, goalData: GoalData): Goal = goalRepository.updateByName(name, goalData)

    @Throws(NotFoundException::class)
    suspend fun deleteGoal(name: String): Goal = goalRepository.deleteByName(name)

    @Throws(NotFoundException::class)
    suspend fun appendSubGoal(name: String, subGoal: Subgoal): Goal {
        val updatedData = goalRepository.findByName(name).data.append(subGoal)
        return goalRepository.updateByName(name, updatedData)
    }

    @Throws(NotFoundException::class)
    suspend fun getSubGoalByIndex(goalName: String, index: Int): Subgoal =
        goalRepository.findByName(goalName).data.subgoals[index]

    @Throws(NotFoundException::class)
    suspend fun replaceSubGoal(name: String, index: Int, subGoal: Subgoal): Goal {
        val updatedData = goalRepository.findByName(name).data.replace(index, subGoal)
        return goalRepository.updateByName(name, updatedData)
    }

    @Throws(NotFoundException::class)
    suspend fun deleteSubGoal(name:String, index: Int): Goal {
        val updatedData = goalRepository.findByName(name).data.remove(index)
        return goalRepository.updateByName(name, updatedData)
    }
}
