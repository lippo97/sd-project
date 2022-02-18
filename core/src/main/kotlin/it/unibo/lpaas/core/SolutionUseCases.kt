package it.unibo.lpaas.core

import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.core.timer.Timer
import it.unibo.lpaas.core.timer.TimerRepository
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Theory
import it.unibo.tuprolog.core.Tuple
import it.unibo.tuprolog.solve.SolverFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

data class GetResultsOptions(
    val skip: Int? = null,
    val limit: Int? = null,
    val within: Duration? = null,
)

@Suppress("all")
class SolutionUseCases<TimerID>(
    private val goalRepository: GoalRepository,
    private val theoryRepository: TheoryRepository,
    private val solutionRepository: SolutionRepository,
    private val timerRepository: TimerRepository<TimerID>,
    private val timer: Timer<TimerID>,
    private val uuidGenerator: Generator<SolutionId>
) {

    private val SOLUTION_MAX_TIMEOUT: Duration = 1.minutes

    private val SOLUTION_DEFAULT_LIMIT: Int = 100

    private val SOLUTION_DEFAULT_SKIP: Int = 0

    companion object Tags {
        val createSolution = Tag("createSolution")

        val getSolution = Tag("getSolution")

        val getSolutionByVersion = Tag("getSolutionByVersion")

        val getResults = Tag("getResults")

        val deleteByName = Tag("deleteByName")
    }

    suspend fun createSolution(name: SolutionId?, data: Solution.Data, every: Duration? = null): Solution {
        val refinedName = name ?: uuidGenerator.generateRandom()
        val solution = _createSolution(refinedName, data)
        every?.let {
            val timerId = timer.setInterval(it.inWholeMilliseconds) {
                updateSolution(refinedName, data)
            }
            timerRepository.create(refinedName, timerId)
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

    suspend fun getResults(
        name: SolutionId,
        solverFactory: SolverFactory,
        options: GetResultsOptions = GetResultsOptions()
    ): Sequence<Result> {
        val (_, data) = solutionRepository.findByName(name)
        val (theoryId, theoryVersion) = data.theoryOptions
        val (_, goalId) = data
        val (skip, limit, within) = options
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
        val timeout = minOf(within ?: SOLUTION_MAX_TIMEOUT, SOLUTION_MAX_TIMEOUT)
            .toLong(DurationUnit.MILLISECONDS)

        val solver = solverFactory.solverOf(theory2p)
        val prevKb = solver.dynamicKb
        return solver
            .solve(composedGoal, timeout = timeout)
            .map(Result::of)
            .andThen(coroutineContext) {
                if (solver.dynamicKb != prevKb) {
                    TheoryUseCases(theoryRepository)
                        .updateTheory(theoryId, Theory.Data(solver.dynamicKb.toImmutableTheory()))
                }
            }
            .drop(skip ?: SOLUTION_DEFAULT_SKIP)
            .take(limit ?: SOLUTION_DEFAULT_LIMIT)
    }

    private fun <T> Sequence<T>.andThen(context: CoroutineContext, action: suspend () -> Unit): Sequence<T> {
        return sequence {
            yieldAll(this@andThen)
            GlobalScope.launch(context) { action() }
        }
    }

    suspend fun deleteSolution(name: SolutionId): Solution {
        timerRepository.safeDeleteByName(name)?.let {
            timer.clear(it)
        }
        return solutionRepository.deleteByName(name)
    }

    private suspend fun updateSolution(name: SolutionId, data: Solution.Data) {
        solutionRepository.updateByName(name, data)
    }
}
