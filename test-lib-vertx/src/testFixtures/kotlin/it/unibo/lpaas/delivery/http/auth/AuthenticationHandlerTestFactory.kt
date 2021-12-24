package it.unibo.lpaas.delivery.http.auth

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import it.unibo.lpaas.auth.Role

object AuthenticationHandlerTestFactory {
    private fun alwaysGrantAndMockGroups(groups: List<String> = listOf()): AuthenticationHandler =
        AuthenticationHandler { ctx ->
            ctx.setUser(
                User.create(
                    JsonObject(),
                    json {
                        obj("accessToken" to obj("groups" to JsonArray(groups)))
                    }
                )
            )
            ctx.next()
        }

    fun alwaysGrantAndMockGroups(vararg groups: String): AuthenticationHandler =
        alwaysGrantAndMockGroups(groups.toList())

    fun alwaysGrantAndMockGroups(vararg groups: Role): AuthenticationHandler =
        alwaysGrantAndMockGroups(groups.map { it.value })

    fun alwaysDeny(): AuthenticationHandler = AuthenticationHandler { ctx -> ctx.fail(401) }
}
