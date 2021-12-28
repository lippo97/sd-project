package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version

class InMemoryTheoryRepository(
    memory: Map<TheoryId, List<Theory>> = mapOf(),
    private val incrementalVersionFactory: () -> IncrementalVersion,
) : TheoryRepository {

    private val baseMemoryRepository: BaseMemoryRepository<TheoryId, List<Theory>, List<Theory>> =
        BaseMemoryRepository(memory, "Theory") { _, theories -> theories }

    private fun make(theoryId: TheoryId, data: Theory.Data): Theory =
        Theory(theoryId, data, incrementalVersionFactory())

    override suspend fun findAll(): List<Theory> =
        baseMemoryRepository.findAll().flatten()

    override suspend fun findByName(name: TheoryId): Theory =
        baseMemoryRepository.findByName(name).first()

    override suspend fun create(name: TheoryId, data: Theory.Data): Theory {
        return make(name, data).also {
            baseMemoryRepository.create(name, listOf(it)).first()
        }
    }

    override suspend fun updateByName(name: TheoryId, data: Theory.Data): Theory {
        return make(name, data).also {
            val updatedList = (listOf(it) + baseMemoryRepository.findByName(name))
                .sortedWith { x, y -> x.version.compareTo(y.version) }
            baseMemoryRepository.updateByName(name, updatedList)
        }
    }

    override suspend fun deleteByName(name: TheoryId): Theory =
        baseMemoryRepository.deleteByName(name).first()

    override suspend fun findByNameAndVersion(name: TheoryId, version: Version): Theory =
        baseMemoryRepository.findByName(name).firstOrNull { it.version == version }
            ?: throw NotFoundException(Pair(name, version), "Theory")

    override suspend fun deleteByNameAndVersion(name: TheoryId, version: Version): Theory {
        return findByNameAndVersion(name, version).also {
            baseMemoryRepository.updateByName(
                name,
                baseMemoryRepository.findByName(name).filter { it.version != version }
            )
        }
    }
}
