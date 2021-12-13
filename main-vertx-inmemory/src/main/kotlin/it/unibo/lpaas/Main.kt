package it.unibo.lpaas

import io.vertx.core.Vertx
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.persistence.InMemoryGoalRepository

fun main() {
    val vertx = Vertx.vertx()
    val controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            goalRepository = InMemoryGoalRepository()
        )
    )

    vertx.createHttpServer()
        .requestHandler(controller.routes())
        .listen(8080).onComplete {
            println("Running...")
        }
}
