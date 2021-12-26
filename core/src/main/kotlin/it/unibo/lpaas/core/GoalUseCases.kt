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
