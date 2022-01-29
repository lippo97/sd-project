package it.unibo.lpaas.core.persistence.repository

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException

interface UpdateByName<Id, Data, Resource> {
    @Throws(ValidationException::class, NotFoundException::class)
    suspend fun updateByName(name: Id, data: Data): Resource
}
