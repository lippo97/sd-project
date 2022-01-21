package it.unibo.lpaas.core.persistence.repository

import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.ValidationException

interface Create<Id, Data, Resource> {
    @Throws(ValidationException::class, DuplicateIdentifierException::class)
    suspend fun create(name: Id, data: Data): Resource
}