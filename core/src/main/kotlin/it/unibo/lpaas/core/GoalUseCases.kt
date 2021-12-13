package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalData
import it.unibo.lpaas.domain.Subgoal

class GoalUseCases(private val goalRepository: GoalRepository) {
    val getAllGoals: UseCase<List<Goal>> = UseCase.of("getAllGoals") {
        goalRepository.findAll()
    }

    fun getGoalByName(name: String): UseCase<Goal> = UseCase.of("getGoalByName") {
        goalRepository.findByName(name)
    }

    fun createGoal(name: String, goalData: GoalData): UseCase<Goal> = UseCase.of("createGoal") {
        goalRepository.create(name, goalData)
    }

    fun replaceGoal(name: String, goalData: GoalData): UseCase<Goal> = UseCase.of("replaceGoal") {
        goalRepository.updateByName(name, goalData)
    }

    fun deleteGoal(name: String): UseCase<Goal> = UseCase.of("deleteGoal") {
        goalRepository.deleteByName(name)
    }

    fun appendSubgoal(name: String, subGoal: Subgoal): UseCase<Goal> = UseCase.of("appendSubgoal") {
        val updatedData = goalRepository.findByName(name).data.append(subGoal)
        goalRepository.updateByName(name, updatedData)
    }

    fun getSubgoalByIndex(goalName: String, index: Int): UseCase<Subgoal> = UseCase.of("getSubgoalByIndex") {
        goalRepository.findByName(goalName).data.subgoals[index]
    }

    fun replaceSubgoal(name: String, index: Int, subGoal: Subgoal): UseCase<Goal> = UseCase.of("replaceSubgoal") {
        val updatedData = goalRepository.findByName(name).data.replace(index, subGoal)
        goalRepository.updateByName(name, updatedData)
    }

    fun deleteSubgoal(name: String, index: Int): UseCase<Goal> = UseCase.of("deleteSubgoal") {
        val updatedData = goalRepository.findByName(name).data.remove(index)
        goalRepository.updateByName(name, updatedData)
    }
}
