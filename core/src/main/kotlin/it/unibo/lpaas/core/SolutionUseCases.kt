package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.tuprolog.core.Tuple
import it.unibo.tuprolog.solve.SolverFactory

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

    suspend fun getResults(name: SolutionId, solverFactory: SolverFactory): Sequence<Result> {
        val (_, data) = solutionRepository.findByName(name)
        val (theoryId, theoryVersion) = data.theoryOptions
        val (_, goalId) = data

        val theory =
            if (theoryVersion != null)
                theoryRepository.findByNameAndVersion(theoryId, theoryVersion)
            else theoryRepository.findByName(theoryId)
        val goal = goalRepository.findByName(goalId)

        val theory2p = theory.data.value
        val subgoals = goal.data.subgoals.map { it.value }
        val composedGoal =
            if (subgoals.size > 1) Tuple.of(goal.data.subgoals.map { it.value })
            else subgoals[0]
        val solver = solverFactory.solverOf(theory2p)
        return solver
            .solve(composedGoal)
            .map(Result::of)
    }
}
