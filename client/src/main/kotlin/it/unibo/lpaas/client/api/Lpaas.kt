package it.unibo.lpaas.client.api

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.streams.ReadStream
import it.unibo.lpaas.client.mio.ServerOptions
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

interface Lpaas {

    fun createTheory(name: TheoryId, data: Theory.Data): Future<Theory>

    fun createGoal(name: GoalId, data: Goal.Data): Future<Goal>

    fun createSolution(name: SolutionId?, data: Solution.Data): Future<Solution>

    fun getResults(name: SolutionId): Pair<ReadStream<Result>, () -> Future<Void>>

    companion object {
        fun of(
            vertx: Vertx,
            client: HttpClient,
            serverOptions: ServerOptions,
            authenticationToken: String
        ): Lpaas = LpaasImpl(vertx, client, serverOptions, authenticationToken)
    }
}
