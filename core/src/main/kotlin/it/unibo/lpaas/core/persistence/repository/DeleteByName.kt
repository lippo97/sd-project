package it.unibo.lpaas.core.persistence.repository

import it.unibo.lpaas.core.exception.NotFoundException

interface DeleteByNameBase<Id, Resource> {
    @Throws(NotFoundException::class)
    suspend fun deleteByName(name: Id): Resource
}

interface DeleteByNameOps<Id, Resource> : DeleteByNameBase<Id, Resource> {

    suspend fun safeDeleteByName(name: Id): Resource? =
        runCatching {
            deleteByName(name)
        }
            .recover { if (it is NotFoundException) null else throw it }
            .getOrNull()
}

interface DeleteByName<Id, Resource> : DeleteByNameBase<Id, Resource>, DeleteByNameOps<Id, Resource>
