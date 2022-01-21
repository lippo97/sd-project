package it.unibo.lpaas.core.persistence.repository

import it.unibo.lpaas.core.exception.NotFoundException

interface FindByName<Id, Resource> {
    @Throws(NotFoundException::class)
    suspend fun findByName(name: Id): Resource

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
