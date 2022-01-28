package it.unibo.lpaas.core.persistence.repository

import it.unibo.lpaas.core.exception.NotFoundException

interface FindByNameBase<Id, Resource> {
    @Throws(NotFoundException::class)
    suspend fun findByName(name: Id): Resource
}

interface FindByNameOps<Id, Resource> : FindByNameBase<Id, Resource> {

    suspend fun safeFindByName(name: Id): Resource? =
        runCatching {
            findByName(name)
        }
            .recover { if (it is NotFoundException) null else throw it }
            .getOrNull()

    suspend fun exists(name: Id): Boolean =
        runCatching {
            findByName(name)
        }
            .map { true }
            .recover { if (it is NotFoundException) false else throw it }
            .getOrThrow()

    @Throws(NotFoundException::class)
    suspend fun unsafeExists(name: Id): Unit =
        runCatching {
            findByName(name)
        }
            .map { }
            .getOrThrow()
}

interface FindByName<Id, Resource> : FindByNameBase<Id, Resource>, FindByNameOps<Id, Resource>
