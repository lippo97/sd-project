package it.unibo.lpaas.persistence

import it.unibo.lpaas.collections.NonEmptyList
import it.unibo.lpaas.collections.nonEmptyListOf
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

class InMemoryTheoryRepository(
    memory: Map<TheoryId, NonEmptyList<Theory>> = mapOf(),
    private val incrementalVersionFactory: () -> IncrementalVersion,
) : TheoryRepository {

    private val baseMemoryRepository: BaseMemoryRepository<TheoryId, NonEmptyList<Theory>, NonEmptyList<Theory>> =
        BaseMemoryRepository(memory, "Theory") { _, theories -> theories }

    private fun make(theoryId: TheoryId, data: Theory.Data): Theory =
        Theory(theoryId, data, incrementalVersionFactory())

    override suspend fun findAll(): List<Theory> =
        baseMemoryRepository.findAll().map { it.head }

    override suspend fun findAllWithVersion(): List<Theory> =
        baseMemoryRepository.findAll().flatten()

    override suspend fun findByName(name: TheoryId): Theory =
        baseMemoryRepository.findByName(name).head

    override suspend fun create(name: TheoryId, data: Theory.Data): Theory {
        return make(name, data).also {
            baseMemoryRepository.create(name, nonEmptyListOf(it)).head
        }
    }

    override suspend fun updateByName(name: TheoryId, data: Theory.Data): Theory {
        val (current, otherVersions) = baseMemoryRepository.findByName(name).snoc

        return Theory(name, data, current.version.next()).also {
            val updatedList = (nonEmptyListOf(it, current) + otherVersions)
            baseMemoryRepository.updateByName(name, updatedList)
        }
    }

    override suspend fun deleteByName(name: TheoryId): Theory =
        baseMemoryRepository.deleteByName(name).head

    override suspend fun findByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory =
        baseMemoryRepository.findByName(name).firstOrNull { it.version == version }
            ?: throw NotFoundException(Pair(name, version), "Theory")

    override suspend fun deleteByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory {
        return findByNameAndVersion(name, version).also {
            val updated = NonEmptyList.fromList(baseMemoryRepository.findByName(name).filter { it.version != version })
            if (updated != null) {
                baseMemoryRepository.updateByName(name, updated)
            } else {
                deleteByName(name)
            }
        }
    }
}

fun TheoryRepository.Companion.inMemory(
    memory: Map<TheoryId, NonEmptyList<Theory>> = mapOf(),
    incrementalVersionFactory: () -> IncrementalVersion,
): TheoryRepository =
    InMemoryTheoryRepository(memory, incrementalVersionFactory)
