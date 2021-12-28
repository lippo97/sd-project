package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

interface TheoryRepository : Repository<TheoryId, Theory.Data, Theory> {

    @Throws(NotFoundException::class)
    suspend fun findByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory

    /**
     * Delete the selected version of the selected theory.
     */
    @Throws(NotFoundException::class)
    suspend fun deleteByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory

    companion object
}
