package it.unibo.lpaas.persistence

import it.unibo.lpaas.collections.NonEmptyList
import it.unibo.lpaas.collections.nonEmptyListOf
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId

class InMemorySolutionRepository(
    memory: Map<SolutionId, NonEmptyList<Solution>> = mapOf(),
    private val incrementalVersionFactory: () -> IncrementalVersion,
) : SolutionRepository {

    private val baseMemoryRepository: BaseMemoryRepository<SolutionId, NonEmptyList<Solution>, NonEmptyList<Solution>> =
        BaseMemoryRepository(memory, "Solution") { _, solutions -> solutions }

    private fun make(solutionId: SolutionId, data: Solution.Data): Solution =
        Solution(solutionId, data, incrementalVersionFactory())

    override suspend fun findByName(name: SolutionId): Solution =
        baseMemoryRepository.findByName(name).head

    override suspend fun findByNameAndVersion(name: SolutionId, version: IncrementalVersion): Solution =
        baseMemoryRepository.findByName(name).firstOrNull { it.version == version }
            ?: throw NotFoundException(Pair(name, version), "Solution")

    override suspend fun create(name: SolutionId, data: Solution.Data): Solution =
        make(name, data).also {
            baseMemoryRepository.create(name, nonEmptyListOf(it)).head
        }

    override suspend fun deleteByName(name: SolutionId): Solution =
        baseMemoryRepository.deleteByName(name).head

    override suspend fun updateByName(name: SolutionId, data: Solution.Data): Solution {
        val (current, otherVersions) = baseMemoryRepository.findByName(name).snoc
        return Solution(name, data, current.version.next()).also {
            val updatedList = (nonEmptyListOf(it, current) + otherVersions)
            baseMemoryRepository.updateByName(name, updatedList)
        }
    }
}

fun SolutionRepository.Companion.inMemory(
    memory: Map<SolutionId, NonEmptyList<Solution>> = mapOf(),
    incrementalVersionFactory: () -> IncrementalVersion,
): SolutionRepository =
    InMemorySolutionRepository(memory, incrementalVersionFactory)
