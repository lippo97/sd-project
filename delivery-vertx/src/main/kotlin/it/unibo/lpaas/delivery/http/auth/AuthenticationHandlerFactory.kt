package it.unibo.lpaas.delivery.http.auth

import io.vertx.ext.web.handler.AuthenticationHandler

object AuthenticationHandlerFactory {
    @JvmStatic
    fun alwaysGrant(): AuthenticationHandler = AuthenticationHandler { ctx -> ctx.next() }
}
