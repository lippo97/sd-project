package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
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

    fun addFactToTheory(name: TheoryId, fact: Fact, beginning: Boolean = true): UseCase<Theory> = TODO()

    fun updateFactInTheory(name: TheoryId, fact: Fact, beginning: Boolean = true): UseCase<Theory> = TODO()
}
