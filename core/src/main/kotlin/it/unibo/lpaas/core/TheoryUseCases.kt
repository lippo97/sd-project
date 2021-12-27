package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

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
    }

    val getAllTheoriesIndex: UseCase<List<TheoryId>> = UseCase.of(Tags.getAllTheoriesIndex) {
        theoryRepository.findAll().map { it.name }
    }

    fun getTheoryByName(name: TheoryId): UseCase<Theory> = TODO()

    fun createTheory(name: TheoryId, data: Theory.Data): UseCase<Theory> = TODO()
}
