package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalData
import kotlin.jvm.Throws

interface GoalRepository {
    suspend fun findAll(): List<Goal>

    @Throws(NotFoundException::class)
    suspend fun findByName(name: String): Goal

    @Throws(ValidationException::class)
    suspend fun create(name: String, goalData: GoalData): Goal

    @Throws(ValidationException::class)
    suspend fun updateByName(name: String, goalData: GoalData): Goal

    @Throws(NotFoundException::class)
    suspend fun deleteByName(name: String): Goal
}
