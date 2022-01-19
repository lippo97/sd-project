package it.unibo.lpaas.core

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.getFactsByFunctor

@Suppress("all")
class TheoryUseCases(private val theoryRepository: TheoryRepository) {

    companion object Tags {
        @JvmStatic
        @get:JvmName("getAllTheories")
        val getAllTheories = Tag("getAllTheories")

        @JvmStatic
        @get:JvmName("getAllTheoriesIndex")
        val getAllTheoriesIndex = Tag("getAllTheoriesIndex")

        @JvmStatic
        @get:JvmName("getAllTheoriesWithVersionIndex")
        val getAllTheoriesWithVersionIndex = Tag("getAllTheoriesWithVersionIndex")

        @JvmStatic
        @get:JvmName("getTheoryByName")
        val getTheoryByName = Tag("getTheoryByName")

        @JvmStatic
        @get:JvmName("createTheory")
        val createTheory = Tag("createTheory")

        @JvmStatic
        @get:JvmName("updateTheory")
        val updateTheory = Tag("updateTheory")

        @JvmStatic
        @get:JvmName("deleteTheory")
        val deleteTheory = Tag("deleteTheory")

        @JvmStatic
        @get:JvmName("getFactsInTheory")
        val getFactsInTheory = Tag("getFactsInTheory")

        @JvmStatic
        @get:JvmName("addFactToTheory")
        val addFactToTheory = Tag("addFactToTheory")

        @JvmStatic
        @get:JvmName("updateFactInTheory")
        val updateFactInTheory = Tag("updateFactInTheory")

        @JvmStatic
        @get:JvmName("getTheoryByVersion")
        val getTheoryByVersion = Tag("getTheoryByVersion")

        @JvmStatic
        @get:JvmName("deleteTheoryByVersion")
        val deleteTheoryByVersion = Tag("deleteTheoryByVersion")

        @JvmStatic
        @get:JvmName("getFactsInTheoryByNameAndVersion")
        val getFactsInTheoryByNameAndVersion = Tag("getFactsInTheoryByNameAndVersion")
    }

    suspend fun getAllTheoriesWithVersionIndex(): List<Pair<TheoryId, Version>> =
        theoryRepository.findAllWithVersion().map { Pair(it.name, it.version) }

    suspend fun getAllTheoriesIndex(): List<TheoryId> =
        theoryRepository.findAll().map(Theory::name)

    suspend fun getTheoryByName(name: TheoryId): Theory =
        theoryRepository.findByName(name)

    suspend fun createTheory(name: TheoryId, data: Theory.Data): Theory =
        theoryRepository.create(name, data)

    suspend fun updateTheory(name: TheoryId, data: Theory.Data): Theory =
        theoryRepository.updateByName(name, data)

    suspend fun deleteTheory(name: TheoryId): Theory =
        theoryRepository.deleteByName(name)

    suspend fun getFactsInTheory(name: TheoryId, functor: Functor): List<Fact> =
        theoryRepository.findByName(name).data.value.getFactsByFunctor(functor).ifEmpty {
            throw NotFoundException(functor, "Fact")
        }

    suspend fun addFactToTheory(name: TheoryId, fact: Fact, beginning: Boolean = true): Theory =
        theoryRepository.run {
            val theory = findByName(name)
            val f = if (beginning) Theory.Data::assertA else Theory.Data::assertZ
            updateByName(name, f(theory.data, fact))
        }

    suspend fun updateFactInTheory(name: TheoryId, fact: Fact, beginning: Boolean = true): Theory =
        theoryRepository.run {
            val theoryData = findByName(name).data.retract(fact.functor, fact.arity)
            val f = if (beginning) Theory.Data::assertA else Theory.Data::assertZ
            updateByName(name, f(theoryData, fact))
        }

    suspend fun getTheoryByNameAndVersion(name: TheoryId, version: IncrementalVersion): Theory =
        theoryRepository.findByNameAndVersion(name, version)

    suspend fun deleteTheoryByVersion(name: TheoryId, version: IncrementalVersion): Theory =
        theoryRepository.deleteByNameAndVersion(name, version)

    suspend fun getFactsInTheoryByNameAndVersion(
        name: TheoryId,
        functor: Functor,
        version: IncrementalVersion
    ): List<Fact> =
        theoryRepository.findByNameAndVersion(name, version).data.value.getFactsByFunctor(functor).ifEmpty {
            throw NotFoundException(functor, "Fact")
        }
}
