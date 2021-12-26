package it.unibo.lpaas.delivery.http

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import it.unibo.lpaas.delivery.http.handler.GoalController

fun interface Controller {
    fun routes(): Router

    companion object {

        const val API_VERSION = "/v1"

        const val GOAL_BASEURL = "/goals"

        fun make(dependencyGraph: DependencyGraph): Controller = Controller {
            Router.router(dependencyGraph.vertx).apply {
                mountSubRouter(
                    GOAL_BASEURL,
                    GoalController.make(
                        dependencyGraph.vertx,
                        dependencyGraph.repositories.goalRepository,
                        dependencyGraph.authenticationHandler,
                        dependencyGraph.mimeMap,
                        dependencyGraph.parsers.goalIdParser,
                        dependencyGraph.authorizationProvider,
                    ).routes()
                )
            }
        }
    }
}
