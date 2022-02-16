package it.unibo.lpaas.delivery.http.handler.dsl

import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.http.databind.BufferSerializer
import it.unibo.lpaas.http.databind.SerializerCollection

class HandlerDSL(
    override val serializerCollection: SerializerCollection<BufferSerializer>,
    override val authHandler: AuthenticationHandler,
    override val authorizationProvider: AuthorizationProvider,
    override val bodyHandler: BodyHandler,
) : AuthenticationDSL,
    AuthorizationDSL,
    SerializerDSL,
    BodyDSL {
    fun Any?.void(): Unit = Unit
}
