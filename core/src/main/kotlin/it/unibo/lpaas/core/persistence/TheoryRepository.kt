package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version

interface TheoryRepository {
    suspend fun findAll(): List<Theory>

    /**
     * Retrieves the latest version of the theory.
     */
    @Throws(NotFoundException::class)
    suspend fun findByName(name: TheoryId): Theory

    @Throws(NotFoundException::class)
    suspend fun findByNameAndVersion(name: TheoryId, version: Version): Theory

    @Throws(ValidationException::class, DuplicateIdentifierException::class)
    suspend fun create(name: TheoryId, data: Theory.Data): Theory

    @Throws(ValidationException::class, NotFoundException::class)
    suspend fun updateByName(name: TheoryId, data: Theory.Data): Theory

    /**
     * Deletes all the versions of the selected theory.
     */
    @Throws(NotFoundException::class)
    suspend fun deleteAllVersionsByName(name: TheoryId): Theory

    /**
     * Delete the selected version of the selected theory.
     */
    @Throws(NotFoundException::class)
    suspend fun deleteByNameAndVersion(name: TheoryId, version: Version): Theory
}
