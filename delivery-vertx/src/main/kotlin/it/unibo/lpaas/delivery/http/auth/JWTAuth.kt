package it.unibo.lpaas.delivery.http.auth

import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions

object JWTAuth {
    fun default(
        vertx: Vertx,
        options: JWTAuthOptions = JWTAuthOptions()
    ): JWTAuth =
        JWTAuth.create(
            vertx,
            options
        )
}
