package it.unibo.lpaas.delivery.http.handler.dsl

import io.vertx.ext.web.handler.AuthenticationHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.SerializerCollection

internal class HandlerDSL(
    override val serializerCollection: SerializerCollection<BufferSerializer>,
    override val authHandler: AuthenticationHandler,
    override val authorizationProvider: AuthorizationProvider,
) : AuthenticationDSL,
    AuthorizationDSL,
    SerializerDSL

