package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
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

    suspend fun createSolution(solutionId: SolutionId, data: Solution.Data): Solution {
        val (theoryOptions, goalId) = data
        val (theoryId, theoryVersion) = theoryOptions

        goalRepository.unsafeExists(goalId)
        if (theoryVersion != null)
            theoryRepository.unsafeExists(theoryId, theoryVersion)
        else
            theoryRepository.unsafeExists(theoryId)

        return solutionRepository.create(solutionId, data)
    }

    suspend fun getSolution(solutionId: SolutionId): Solution = solutionRepository.findByName(solutionId)
}
