package it.unibo.lpaas.core

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal

class GoalUseCases(private val goalRepository: GoalRepository) {
    val getAllGoals: UseCase<List<Goal>> = UseCase.of("getAllGoals") {
        goalRepository.findAll()
    }

    val getAllGoalsIndex: UseCase<List<GoalId>> = UseCase.of("getAllGoalsIndex") {
        goalRepository.findAll().map { it.name }
    }

    fun getGoalByName(name: GoalId): UseCase<Goal> = UseCase.of("getGoalByName") {
        goalRepository.findByName(name)
    }

    fun createGoal(name: GoalId, data: Goal.Data): UseCase<Goal> = UseCase.of("createGoal") {
        goalRepository.create(name, data)
    }

    fun replaceGoal(name: GoalId, data: Goal.Data): UseCase<Goal> = UseCase.of("replaceGoal") {
        goalRepository.updateByName(name, data)
    }

    fun deleteGoal(name: GoalId): UseCase<Goal> = UseCase.of("deleteGoal") {
        goalRepository.deleteByName(name)
    }

    fun appendSubgoal(name: GoalId, subGoal: Subgoal): UseCase<Goal> = UseCase.of("appendSubgoal") {
        val updatedData = goalRepository.findByName(name).data.append(subGoal)
        goalRepository.updateByName(name, updatedData)
    }

    fun getSubgoalByIndex(name: GoalId, index: Int): UseCase<Subgoal> = UseCase.of("getSubgoalByIndex") {
        goalRepository.findByName(name).data.subgoals.run {
            if (index < size) this[index]
            else throw NotFoundException("$name/$index", "Goal")
        }
    }

    fun replaceSubgoal(name: GoalId, index: Int, subGoal: Subgoal): UseCase<Goal> = UseCase.of("replaceSubgoal") {
        val updatedData = goalRepository.findByName(name).data.replace(index, subGoal)
        goalRepository.updateByName(name, updatedData)
    }

    fun deleteSubgoal(name: GoalId, index: Int): UseCase<Goal> = UseCase.of("deleteSubgoal") {
        val updatedData = goalRepository.findByName(name).data.remove(index)
        goalRepository.updateByName(name, updatedData)
    }
}
