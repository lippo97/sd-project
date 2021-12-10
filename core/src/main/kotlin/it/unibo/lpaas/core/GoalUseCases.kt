package it.unibo.lpaas.core

import it.unibo.lpaas.core.repository.GoalRepository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalData

suspend fun GoalRepository.getAllGoals(): List<Goal> = findAll()

suspend fun GoalRepository.getGoalByName(name: String): Goal = findByName(name)

suspend fun GoalRepository.createGoal(name: String, goalData: GoalData): Goal = create(name, goalData)
