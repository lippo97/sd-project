package it.unibo.lpaas.client.api

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import it.unibo.lpaas.authentication.domain.Credentials
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

@Suppress("TooManyFunctions")
interface Lpaas {

    /**
     * GOAL use cases
     */
    fun getAllGoalsIndex(): Future<List<GoalId>>

    fun getGoalByName(name: GoalId): Future<Goal>

    fun createGoal(name: GoalId, data: Goal.Data): Future<Goal>

    fun replaceGoal(name: GoalId, data: Goal.Data): Future<Goal>

    fun deleteGoal(name: GoalId): Future<Unit>

    fun appendSubgoal(name: GoalId, subGoal: Subgoal): Future<Goal>

    fun getSubgoalByIndex(name: GoalId, index: Int): Future<Subgoal>

    fun replaceSubgoal(name: GoalId, index: Int, subGoal: Subgoal): Future<Goal>

    fun deleteSubgoal(name: GoalId, index: Int): Future<Unit>

    /**
     * THEORY use cases
     */
    fun getAllTheoriesIndex(): Future<List<TheoryId>>

    fun getTheoryByName(name: TheoryId): Future<Theory>

    fun createTheory(name: TheoryId, data: Theory.Data): Future<Theory>

    fun updateTheory(name: TheoryId, data: Theory.Data): Future<Theory>

    fun deleteTheory(name: TheoryId): Future<Unit>

    fun getFactsInTheory(name: TheoryId, functor: Functor): Future<List<Fact>>

    fun addFactToTheory(name: TheoryId, fact: Fact, beginning: Boolean = true): Future<Theory>

    fun updateFactInTheory(name: TheoryId, fact: Fact, beginning: Boolean = true): Future<Theory>

    fun getTheoryByNameAndVersion(name: TheoryId, version: IncrementalVersion): Future<Theory>

    fun deleteTheoryByVersion(name: TheoryId, version: IncrementalVersion): Future<Unit>

    fun getFactsInTheoryByNameAndVersion(
        name: TheoryId,
        functor: Functor,
        version: IncrementalVersion
    ): Future<List<Fact>>

    /**
     * SOLUTION use cases
     */

    fun getSolution(name: SolutionId): Future<Solution>

    fun getSolutionByVersion(name: SolutionId, version: IncrementalVersion): Future<Solution>

    fun deleteSolution(name: SolutionId): Future<Unit>

    fun createSolution(name: SolutionId?, data: Solution.Data): Future<Solution>

    fun getResults(name: SolutionId): Future<ResultStream>

    companion object {
        fun of(
            vertx: Vertx,
            client: HttpClient,
            serverOptions: ServerOptions,
            credentials: Credentials
        ): Lpaas = LpaasImpl(vertx, client, serverOptions, serverOptions, credentials)

        fun of(
            vertx: Vertx,
            client: HttpClient,
            serverOptions: ServerOptions,
            authServerOptions: ServerOptions,
            credentials: Credentials
        ): Lpaas = LpaasImpl(vertx, client, serverOptions, authServerOptions, credentials)
    }
}
