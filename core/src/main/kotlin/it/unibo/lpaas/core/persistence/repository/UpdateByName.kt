package it.unibo.lpaas.core.persistence.repository

import it.unibo.lpaas.core.exception.NotFoundException

interface UpdateByName<Id, Data, Resource> {
    @Throws(NotFoundException::class)
    suspend fun updateByName(name: Id, data: Data): Resource
}
