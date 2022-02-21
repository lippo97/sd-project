package it.unibo.lpaas.core.persistence.repository

import it.unibo.lpaas.core.exception.DuplicateIdentifierException

interface Create<Id, Data, Resource> {
    @Throws(DuplicateIdentifierException::class)
    suspend fun create(name: Id, data: Data): Resource
}
