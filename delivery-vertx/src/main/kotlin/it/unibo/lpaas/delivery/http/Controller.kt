package it.unibo.lpaas.delivery.http

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.HttpException
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.core.exception.NonFatalError
import it.unibo.lpaas.delivery.http.handler.GoalController
import it.unibo.lpaas.delivery.http.handler.SolutionController
import it.unibo.lpaas.delivery.http.handler.TheoryController
import it.unibo.lpaas.delivery.http.handler.handleNonFatal

fun interface Controller {
    fun routes(): Router

    data class AuthOptions(
        val authenticationHandler: AuthenticationHandler,
        val authorizationProvider: AuthorizationProvider,
    )

    companion object {

        const val API_VERSION = "/v1"

        const val GOAL_BASEURL = "/goals"

        const val THEORY_BASEURL = "/theories"

        const val SOLUTION_BASEURL = "/solutions"

        @JvmStatic
        fun <TimerID> make(dependencyGraph: DependencyGraph<TimerID>): Controller = Controller {
            val (
                vertx,
                timerDependencies,
                serializers,
                goalDependencies,
                theoryDependencies,
                solutionDependencies,
                authOptions
            ) = dependencyGraph

            Router.router(vertx).apply {
                mountSubRouter(
                    GOAL_BASEURL,
                    GoalController.make(
                        vertx = vertx,
                        goalDependencies = goalDependencies,
                        authOptions = authOptions,
                        serializers = serializers,
                    ).routes()
                )
                mountSubRouter(
                    THEORY_BASEURL,
                    TheoryController.make(
                        vertx = vertx,
                        theoryDependencies = theoryDependencies,
                        authOptions = authOptions,
                        serializers = serializers
                    ).routes()
                )

                mountSubRouter(
                    SOLUTION_BASEURL,
                    SolutionController.make(
                        vertx = vertx,
                        solutionDependencies = solutionDependencies,
                        timerDependencies = timerDependencies,
                        goalRepository = goalDependencies.goalRepository,
                        theoryRepository = theoryDependencies.theoryRepository,
                        incrementalVersionParser = theoryDependencies.incrementalVersionParser,
                        authOptions = authOptions,
                        serializers = serializers,
                    ).routes()
                )

                route("/*")
                    .nonFatalHandler()
                    .failureHandler { ctx ->
                        ctx.failure()?.printStackTrace()
                        ctx.response()
                            .setStatusCode(HTTPStatusCode.INTERNAL_SERVER_ERROR)
                            .end()
                    }
            }
        }

        private fun Route.nonFatalHandler(): Route = failureHandler { ctx ->
            when (val failure = ctx.failure()) {
                is NonFatalError -> ctx.handleNonFatal(failure)
                is HttpException -> ctx.response()
                    .setStatusCode(failure.statusCode)
                    .end()
                else -> if (ctx.statusCode() != HTTPStatusCode.INTERNAL_SERVER_ERROR.code)
                    ctx.response()
                        .setStatusCode(ctx.statusCode())
                        .end()
                else {
                    ctx.fail(failure)
                }
            }
        }
    }
}
