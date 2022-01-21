package it.unibo.lpaas.core.persistence.repository

import it.unibo.lpaas.core.exception.NotFoundException

interface DeleteByName<Id, Resource> {
    @Throws(NotFoundException::class)
    suspend fun deleteByName(name: Id): Resource
}
