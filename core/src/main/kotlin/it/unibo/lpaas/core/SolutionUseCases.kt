package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.core.timer.Timer
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Theory
import it.unibo.tuprolog.core.Tuple
import it.unibo.tuprolog.solve.SolverFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onCompletion

@Suppress("all")
class SolutionUseCases<TimerID>(
    private val goalRepository: GoalRepository,
    private val theoryRepository: TheoryRepository,
    private val solutionRepository: SolutionRepository,
    private val timer: Timer<TimerID>,
) {

    companion object Tags {
        val createSolution = Tag("createSolution")

        val getSolution = Tag("getSolution")

        val getSolutionByVersion = Tag("getSolutionByVersion")
    }

    suspend fun createSolution(name: SolutionId, data: Solution.Data, every: Long? = null): Solution {
        val solution = _createSolution(name, data)
        every?.let {
            timer.setInterval(it) {
                _createSolution(name, data)
            }
        }
        return solution
    }

    private suspend fun _createSolution(name: SolutionId, data: Solution.Data): Solution {
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

    suspend fun getResults(name: SolutionId, solverFactory: SolverFactory): Flow<Result> {
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
        val prevKb = solver.dynamicKb
        return solver
            .solve(composedGoal)
            .map(Result::of)
            .asFlow()
            .onCompletion {
                if (solver.dynamicKb != prevKb) {
                    TheoryUseCases(theoryRepository)
                        .updateTheory(theoryId, Theory.Data(solver.dynamicKb))
                }
            }
    }
}
