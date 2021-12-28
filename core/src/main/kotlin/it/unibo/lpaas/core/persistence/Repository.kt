package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException

interface Repository<Id, Data, Resource> {

    suspend fun findAll(): List<Resource>

    @Throws(NotFoundException::class)
    suspend fun findByName(name: Id): Resource

    @Throws(ValidationException::class, DuplicateIdentifierException::class)
    suspend fun create(name: Id, data: Data): Resource

    @Throws(ValidationException::class, NotFoundException::class)
    suspend fun updateByName(name: Id, data: Data): Resource

    @Throws(NotFoundException::class)
    suspend fun deleteByName(name: Id): Resource
}
