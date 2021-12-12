package it.unibo.lpaas.delivery.http

import io.vertx.ext.web.Router

fun interface Controller {
    fun routes(): Router

    companion object {
        fun make(dependencyGraph: DependencyGraph): Controller = Controller {
            Router.router(dependencyGraph.vertx).apply { }
        }
    }
}
