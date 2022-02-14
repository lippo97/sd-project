package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.descending
import org.litote.kmongo.eq

class MongoSolutionRepository(
    private val solutionCollection: CoroutineCollection<Solution>,
    private val incrementalVersionFactory: () -> IncrementalVersion,
) : SolutionRepository {
    override suspend fun findByNameAndVersion(name: SolutionId, version: IncrementalVersion): Solution =
        solutionCollection.findOne(and(Solution::name eq name, Solution::version eq version))
            ?: throw NotFoundException(name, "Solution")

    override suspend fun findByName(name: SolutionId): Solution =
        solutionCollection.find(Solution::name eq name).sort(descending(Solution::version)).first()
            ?: throw NotFoundException(name, "Solution")

    override suspend fun create(name: SolutionId, data: Solution.Data): Solution =
        create(name, data, incrementalVersionFactory())

    override suspend fun deleteByName(name: SolutionId): Solution =
        findByName(name).also {
            solutionCollection.deleteMany(Solution::name eq name)
        }

    override suspend fun updateByName(name: SolutionId, data: Solution.Data): Solution =
        create(name, data, findByName(name).version.next())

    private suspend fun create(name: SolutionId, data: Solution.Data, version: IncrementalVersion): Solution =
        Solution(name, data, version).also { solutionCollection.insertOne(it) }
}

fun SolutionRepository.Companion.mongo(
    solutionCollection: CoroutineCollection<Solution>,
    incrementalVersionFactory: () -> IncrementalVersion,
): SolutionRepository = MongoSolutionRepository(solutionCollection, incrementalVersionFactory)
