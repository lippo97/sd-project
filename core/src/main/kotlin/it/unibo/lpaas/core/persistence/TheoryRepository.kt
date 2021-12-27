package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

interface TheoryRepository {
    suspend fun findAll(): List<Theory>

    @Throws(NotFoundException::class)
    suspend fun findByName(name: TheoryId): Theory

    @Throws(ValidationException::class, DuplicateIdentifierException::class)
    suspend fun create(name: TheoryId, data: Theory.Data): Theory

    @Throws(ValidationException::class, NotFoundException::class)
    suspend fun updateByName(name: TheoryId, data: Theory.Data): Theory

    @Throws(NotFoundException::class)
    suspend fun deleteByName(name: TheoryId): Theory
}
