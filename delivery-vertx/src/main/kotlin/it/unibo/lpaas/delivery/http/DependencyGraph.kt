package it.unibo.lpaas.delivery.http

import io.vertx.core.Vertx
import it.unibo.lpaas.core.repository.GoalRepository

class DependencyGraph(
    val vertx: Vertx,
    val goalRepository: GoalRepository,
)
