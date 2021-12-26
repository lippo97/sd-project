package it.unibo.lpaas.delivery.http

import io.vertx.core.Vertx
import io.vertx.ext.web.handler.AuthenticationHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.delivery.StringParser
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.domain.GoalId

class Parsers(
    val goalIdParser: StringParser<GoalId>
)

class Repositories(
    val goalRepository: GoalRepository,
)

class DependencyGraph(
    val vertx: Vertx,
    val mimeMap: MimeMap<BufferSerializer>,
    val repositories: Repositories,
    val authenticationHandler: AuthenticationHandler,
    val authorizationProvider: AuthorizationProvider,
    val parsers: Parsers,
)
