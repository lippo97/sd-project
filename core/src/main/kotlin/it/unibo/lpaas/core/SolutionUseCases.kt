package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId

@Suppress("all")
class SolutionUseCases(
    private val goalRepository: GoalRepository,
    private val theoryRepository: TheoryRepository,
    private val solutionRepository: SolutionRepository,
) {

    companion object Tags {
        val createSolution = Tag("createSolution")

        val getSolution = Tag("getSolution")

        val getSolutionByVersion = Tag("getSolutionByVersion")
    }

    suspend fun createSolution(name: SolutionId, data: Solution.Data): Solution {
        val (theoryOptions, goalId) = data
        val (theoryId, theoryVersion) = theoryOptions

        goalRepository.unsafeExists(goalId)
        if (theoryVersion != null)
            theoryRepository.unsafeExists(theoryId, theoryVersion)
        else
            theoryRepository.unsafeExists(theoryId)

        return solutionRepository.create(name, data)
    }

    suspend fun getSolution(name: SolutionId): Solution = solutionRepository.findByName(name)

    suspend fun getSolutionByVersion(name: SolutionId, version: IncrementalVersion): Solution =
        solutionRepository.findByNameAndVersion(name, version)
}
