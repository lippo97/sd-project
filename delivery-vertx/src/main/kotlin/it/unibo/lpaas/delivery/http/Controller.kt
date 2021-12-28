package it.unibo.lpaas.delivery.http

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.AuthenticationHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.delivery.http.handler.GoalController
import it.unibo.lpaas.delivery.http.handler.TheoryController

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
                        mimeMap = mimeMap,
                    ).routes()
                )
                mountSubRouter(
                    THEORY_BASEURL,
                    TheoryController.make(
                        vertx = vertx,
                        theoryDependencies = theoryDependencies,
                        authOptions = authOptions,
                        mimeMap = mimeMap
                    ).routes()
                )
            }
        }
    }
}
