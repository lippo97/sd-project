package it.unibo.lpaas.delivery.http

import io.vertx.ext.web.Router
import it.unibo.lpaas.delivery.http.handler.goalHandler

fun interface Controller {
    fun routes(): Router

    companion object {
        fun make(dependencyGraph: DependencyGraph): Controller = Controller {
            Router.router(dependencyGraph.vertx).apply {
                mountSubRouter(
                    "/goal",
                    goalHandler(
                        dependencyGraph.vertx,
                        dependencyGraph.goalRepository,
                        dependencyGraph.serializers,
                        dependencyGraph.factories.goalIdFactory,
                    )
                )
            }
        }
    }
}
