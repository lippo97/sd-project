package it.unibo.lpaas.delivery.http.auth

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.web.handler.AuthenticationHandler

object AuthenticationHandlerFactory {
    @JvmStatic
    fun alwaysGrant(): AuthenticationHandler = AuthenticationHandler { ctx ->
        ctx.setUser(
            User.create(
                JsonObject(),
                JsonObject().put("accessToken", JsonObject().put("groups", JsonArray().add("client")))
            )
        )
        ctx.next()
    }
}
