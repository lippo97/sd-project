package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.descending
import org.litote.kmongo.eq

class MongoTheoryRepository(
    private val theoryCollection: CoroutineCollection<Theory>
) : TheoryRepository {
    override suspend fun findAll(): List<Theory> =
        theoryCollection.find().toList()

    override suspend fun findByName(name: TheoryId): Theory =
        theoryCollection.find(Theory::name eq name).sort(descending(Theory::version)).first()
            ?: throw NotFoundException(name, "Theory")

    override suspend fun findByNameAndVersion(name: TheoryId, version: Version): Theory =
        theoryCollection.findOne(and(Theory::name eq name, Theory::version eq version))
            ?: throw NotFoundException(name, "Theory")

    override suspend fun create(name: TheoryId, data: Theory.Data): Theory =
        Theory(name, data, Version.incremental).also { theoryCollection.insertOne(it) }

    override suspend fun updateByName(name: TheoryId, data: Theory.Data): Theory {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllVersionsByName(name: TheoryId): Theory {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByNameAndVersion(name: TheoryId, version: Version): Theory {
        TODO("Not yet implemented")
    }
}
