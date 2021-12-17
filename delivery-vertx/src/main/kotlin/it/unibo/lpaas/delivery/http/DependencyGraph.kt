package it.unibo.lpaas.delivery.http

import io.vertx.core.Vertx
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.domain.GoalId

class Factories(
    val goalIdFactory: (String) -> GoalId
)

class DependencyGraph(
    val vertx: Vertx,
    val serializers: Map<MimeType, BufferSerializer>,
    val goalRepository: GoalRepository,
    val factories: Factories,
)
