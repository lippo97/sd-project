package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.repository.Repository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

interface TheoryRepository : Repository<TheoryId, Theory.Data, Theory> {

    suspend fun findAllWithVersion(): List<Theory>

    @Throws(NotFoundException::class)
    suspend fun findByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory

    suspend fun exists(name: TheoryId, version: IncrementalVersion): Boolean =
        runCatching {
            findByNameAndVersion(name, version)
        }
            .map { true }
            .recover { if (it is NotFoundException) false else throw it }
            .getOrThrow()

    @Throws(NotFoundException::class)
    suspend fun unsafeExists(name: TheoryId, version: IncrementalVersion): Unit =
        runCatching {
            findByNameAndVersion(name, version)
        }
            .map { }
            .getOrThrow()

    /**
     * Delete the selected version of the selected theory.
     */
    @Throws(NotFoundException::class)
    suspend fun deleteByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory

    companion object
}
