package it.unibo.lpaas.delivery.http.auth

import io.vertx.core.Vertx
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions

object JWTAuthFactory {
    fun hs256SecretBased(vertx: Vertx, secret: String): JWTAuth = JWTAuth.create(
        vertx,
        JWTAuthOptions()
            .addPubSecKey(
                PubSecKeyOptions()
                    .setAlgorithm("HS256")
                    .setBuffer(secret)
            )
    )
}
