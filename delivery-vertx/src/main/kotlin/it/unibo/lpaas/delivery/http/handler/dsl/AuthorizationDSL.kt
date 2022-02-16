package it.unibo.lpaas.delivery.http.handler.dsl

import io.vertx.ext.auth.authorization.OrAuthorization
import io.vertx.ext.auth.authorization.RoleBasedAuthorization
import io.vertx.ext.auth.jwt.authorization.MicroProfileAuthorization
import io.vertx.ext.web.Route
import io.vertx.ext.web.handler.AuthorizationHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.Tag

interface AuthorizationDSL {
    val authorizationProvider: AuthorizationProvider

    /**
     * Creates an authorization middleware based on the provided
     * [AuthorizationProvider].
     */
    fun Route.authorizationHandler(tag: Tag): Route {
        val authorizationHandler = AuthorizationHandler.create(
            OrAuthorization.create().apply {
                authorizationProvider.authorizedRoles(tag)
                    .map(Role::value)
                    .map(RoleBasedAuthorization::create)
                    .forEach(::addAuthorization)
            }
        )
            .addAuthorizationProvider(MicroProfileAuthorization.create())

        return handler(authorizationHandler)
    }
}
