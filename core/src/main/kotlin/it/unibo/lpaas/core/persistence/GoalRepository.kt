package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import kotlin.jvm.Throws

interface GoalRepository {
    suspend fun findAll(): List<Goal>

    @Throws(NotFoundException::class)
    suspend fun findByName(name: GoalId): Goal

    @Throws(ValidationException::class, NotFoundException::class)
    suspend fun create(name: GoalId, data: Goal.Data): Goal

    @Throws(ValidationException::class, NotFoundException::class)
    suspend fun updateByName(name: GoalId, data: Goal.Data): Goal

    @Throws(NotFoundException::class)
    suspend fun deleteByName(name: GoalId): Goal
}
