package it.unibo.lpaas.delivery.http

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.HttpException
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.core.exception.NonFatalError
import it.unibo.lpaas.delivery.http.handler.GoalController
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

        @JvmStatic
        fun make(dependencyGraph: DependencyGraph): Controller = Controller {
            val (vertx, mimeMap, goalDependencies, theoryDependencies, authOptions) = dependencyGraph

            Router.router(vertx).apply {
                mountSubRouter(
                    GOAL_BASEURL,
                    GoalController.make(
                        vertx = vertx,
                        goalDependencies = goalDependencies,
                        authOptions = authOptions,
                        serializerCollection = mimeMap,
                    ).routes()
                )
                mountSubRouter(
                    THEORY_BASEURL,
                    TheoryController.make(
                        vertx = vertx,
                        theoryDependencies = theoryDependencies,
                        authOptions = authOptions,
                        serializerCollection = mimeMap
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
