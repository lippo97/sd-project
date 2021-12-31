package it.unibo.lpaas.delivery.http

import io.vertx.core.Vertx
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.delivery.StringParser
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.TheoryId

class GoalDependencies(
    val goalRepository: GoalRepository,
    val goalIdParser: StringParser<GoalId>,
)

class TheoryDependencies(
    val theoryRepository: TheoryRepository,
    val theoryIdParser: StringParser<TheoryId>,
    val functorParser: StringParser<Functor>,
    val incrementalVersionParser: StringParser<IncrementalVersion>,
)

data class DependencyGraph(
    val vertx: Vertx,
    val mimeMap: MimeMap<BufferSerializer>,
    val goalDependencies: GoalDependencies,
    val theoryDependencies: TheoryDependencies,
    val authOptions: Controller.AuthOptions,
)
