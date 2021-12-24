package it.unibo.lpaas.delivery.http

import io.vertx.ext.web.Router
import it.unibo.lpaas.delivery.http.handler.goalHandler

fun interface Controller {
    fun routes(): Router

    companion object {

        const val GOAL_BASEURL = "/goals"

        fun make(dependencyGraph: DependencyGraph): Controller = Controller {
            Router.router(dependencyGraph.vertx).apply {
                mountSubRouter(
                    GOAL_BASEURL,
                    goalHandler(
                        dependencyGraph.vertx,
                        dependencyGraph.goalRepository,
                        dependencyGraph.authenticationHandler,
                        dependencyGraph.mimeMap,
                        dependencyGraph.parsers.goalIdParser,
                        dependencyGraph.rbac
                    )
                )
            }
        }
    }
}
