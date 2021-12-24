package it.unibo.lpaas.core

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal

class GoalUseCases(private val goalRepository: GoalRepository) {

    companion object Tags {
        val getAllGoals = Tag("getAllGoals")

        val getAllGoalsIndex = Tag("getAllGoalsIndex")

        val getGoalByName = Tag("getGoalByName")

        val createGoal = Tag("createGoal")

        val replaceGoal = Tag("replaceGoal")

        val deleteGoal = Tag("deleteGoal")

        val appendSubgoal = Tag("appendSubgoal")

        val getSubgoalByIndex = Tag("getSubgoalByIndex")

        val replaceSubgoal = Tag("replaceSubgoal")

        val deleteSubgoal = Tag("deleteSubgoal")
    }

    val getAllGoals: UseCase<List<Goal>> = UseCase.of(Tags.getAllGoals) {
        goalRepository.findAll()
    }

    val getAllGoalsIndex: UseCase<List<GoalId>> = UseCase.of(Tags.getAllGoalsIndex) {
        goalRepository.findAll().map { it.name }
    }

    fun getGoalByName(name: GoalId): UseCase<Goal> = UseCase.of(Tags.getGoalByName) {
        goalRepository.findByName(name)
    }

    fun createGoal(name: GoalId, data: Goal.Data): UseCase<Goal> = UseCase.of(Tags.createGoal) {
        goalRepository.create(name, data)
    }

    fun replaceGoal(name: GoalId, data: Goal.Data): UseCase<Goal> = UseCase.of(Tags.replaceGoal) {
        goalRepository.updateByName(name, data)
    }

    fun deleteGoal(name: GoalId): UseCase<Goal> = UseCase.of(Tags.deleteGoal) {
        goalRepository.deleteByName(name)
    }

    fun appendSubgoal(name: GoalId, subGoal: Subgoal): UseCase<Goal> = UseCase.of(Tags.appendSubgoal) {
        val updatedData = goalRepository.findByName(name).data.append(subGoal)
        goalRepository.updateByName(name, updatedData)
    }

    fun getSubgoalByIndex(name: GoalId, index: Int): UseCase<Subgoal> = UseCase.of(Tags.getSubgoalByIndex) {
        goalRepository.findByName(name).data.subgoals.run {
            if (index < size) this[index]
            else throw NotFoundException("$name/$index", "Goal")
        }
    }

    fun replaceSubgoal(name: GoalId, index: Int, subGoal: Subgoal): UseCase<Goal> = UseCase.of(Tags.replaceSubgoal) {
        val updatedData = goalRepository.findByName(name).data.replace(index, subGoal)
        goalRepository.updateByName(name, updatedData)
    }

    fun deleteSubgoal(name: GoalId, index: Int): UseCase<Goal> = UseCase.of(Tags.deleteSubgoal) {
        val updatedData = goalRepository.findByName(name).data.remove(index)
        goalRepository.updateByName(name, updatedData)
    }
}
