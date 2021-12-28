package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
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

    val getAllTheoriesIndex: UseCase<List<TheoryId>> = UseCase.of(Tags.getAllTheoriesIndex) {
        theoryRepository.findAll().map { it.name }
    }

    fun getTheoryByName(name: TheoryId): UseCase<Theory> = UseCase.of(Tags.getTheoryByName) {
        theoryRepository.findByName(name)
    }

    fun createTheory(name: TheoryId, data: Theory.Data): UseCase<Theory> = UseCase.of(Tags.createTheory) {
        theoryRepository.create(name, data)
    }

    fun updateTheory(name: TheoryId, data: Theory.Data): UseCase<Theory> = UseCase.of(Tags.updateTheory) {
        theoryRepository.updateByName(name, data)
    }

    fun deleteTheory(name: TheoryId): UseCase<Theory> = UseCase.of(Tags.deleteTheory) {
        theoryRepository.deleteAllVersionsByName(name)
    }

    fun getFactsInTheory(name: TheoryId, functor: Functor): UseCase<List<Fact>> = UseCase.of(Tags.getFactsInTheory) {
        theoryRepository.findByName(name).data.value.getFactsByFunctor(functor)
    }

    fun addFactToTheory(name: TheoryId, fact: Fact, beginning: Boolean = true): UseCase<Theory> =
        UseCase.of(Tags.addFactToTheory) {
            theoryRepository.run {
                val theory = findByName(name)
                val f = if (beginning) Theory.Data::assertA else Theory.Data::assertZ
                updateByName(name, f(theory.data, fact))
            }
        }

    fun updateFactInTheory(name: TheoryId, fact: Fact, beginning: Boolean = true): UseCase<Theory> =
        UseCase.of(Tags.updateFactInTheory) {
            theoryRepository.run {
                val theoryData = findByName(name).data.retract(fact.functor, fact.arity)
                val f = if (beginning) Theory.Data::assertA else Theory.Data::assertZ
                updateByName(name, f(theoryData, fact))
            }
        }

    fun getTheoryByNameAndVersion(name: TheoryId, version: Version): UseCase<Theory> =
        UseCase.of(Tags.getTheoryByVersion) {
            theoryRepository.findByNameAndVersion(name, version)
        }

    fun deleteTheoryByVersion(name: TheoryId, version: Version): UseCase<Theory> =
        UseCase.of(Tags.deleteTheoryByVersion) {
            theoryRepository.deleteByNameAndVersion(name, version)
        }

    fun getFactsInTheoryByNameAndVersion(name: TheoryId, functor: Functor, version: Version): UseCase<List<Fact>> =
        UseCase.of(Tags.getFactsInTheoryByNameAndVersion) {
            theoryRepository.findByNameAndVersion(name, version).data.value.getFactsByFunctor(functor)
        }
}
