package it.unibo.lpaas.persistence

import com.mongodb.client.model.Filters
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import org.bson.BsonString
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.first
import org.litote.kmongo.group
import org.litote.kmongo.sort

class MongoTheoryRepository(
    private val theoryCollection: CoroutineCollection<Theory>,
    private val incrementalVersionFactory: () -> IncrementalVersion,
) : TheoryRepository {
    override suspend fun findAll(): List<Theory> {
        data class Result(val resultId: ObjectId)
        val ids = theoryCollection.aggregate<Result>(
            sort(descending(Theory::version)),
            group(
                Theory::name,
                Result::resultId first BsonString("\$_id"),
            ),
        )
            .toList()
            .map { it.resultId }
        return theoryCollection.find(Filters.`in`("_id", ids)).toList()
    }

    override suspend fun findByName(name: TheoryId): Theory =
        theoryCollection.find(Theory::name eq name).sort(descending(Theory::version)).first()
            ?: throw NotFoundException(name, "Theory")

    override suspend fun findAllWithVersion(): List<Theory> =
        theoryCollection.find().toList()

    override suspend fun findByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory =
        theoryCollection.findOne(and(Theory::name eq name, Theory::version eq version))
            ?: throw NotFoundException(name, "Theory")

    override suspend fun create(name: TheoryId, data: Theory.Data): Theory =
        create(name, data, incrementalVersionFactory())

    override suspend fun updateByName(name: TheoryId, data: Theory.Data): Theory =
        create(name, data, findByName(name).version.next())

    override suspend fun deleteByName(name: TheoryId): Theory =
        findByName(name).also {
            theoryCollection.deleteMany(Theory::name eq name)
        }

    override suspend fun deleteByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory =
        theoryCollection.findOneAndDelete(and(Theory::name eq name, Theory::version eq version))
            ?: throw NotFoundException(name, "Theory")

    private suspend fun create(name: TheoryId, data: Theory.Data, version: IncrementalVersion): Theory =
        Theory(name, data, version).also { theoryCollection.insertOne(it) }
}

fun TheoryRepository.Companion.mongo(
    theoryCollection: CoroutineCollection<Theory>,
    incrementalVersionFactory: () -> IncrementalVersion,
): TheoryRepository = MongoTheoryRepository(theoryCollection, incrementalVersionFactory)
