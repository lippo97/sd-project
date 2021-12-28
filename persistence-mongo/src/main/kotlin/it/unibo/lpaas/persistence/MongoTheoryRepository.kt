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
        create(name, data, Version.incrementalZero)

    // TODO version must have next() method
    override suspend fun updateByName(name: TheoryId, data: Theory.Data): Theory =
        theoryCollection.run {
            val theory = findByName(name)
            create(name, theory.data, theory.version)
        }

    override suspend fun deleteByName(name: TheoryId): Theory {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByNameAndVersion(name: TheoryId, version: Version): Theory =
        TODO("Not yet implemented")

    private suspend fun create(name: TheoryId, data: Theory.Data, version: Version): Theory =
        Theory(name, data, version).also { theoryCollection.insertOne(it) }
}
