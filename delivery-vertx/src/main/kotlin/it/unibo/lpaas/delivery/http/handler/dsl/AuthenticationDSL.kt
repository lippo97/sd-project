package it.unibo.lpaas.delivery.http.handler.dsl

import io.vertx.ext.web.Route
import io.vertx.ext.web.handler.AuthenticationHandler

internal interface AuthenticationDSL {

    val authHandler: AuthenticationHandler

    fun Route.authenticationHandler(): Route =
        handler(authHandler)

    companion object {
        fun of(authHandler: AuthenticationHandler): AuthenticationDSL = object : AuthenticationDSL {
            override val authHandler: AuthenticationHandler = authHandler
        }
    }
}
