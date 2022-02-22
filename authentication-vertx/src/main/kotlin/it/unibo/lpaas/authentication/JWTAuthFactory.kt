package it.unibo.lpaas.authentication

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
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

    fun rs256(vertx: Vertx, publicKey: String): JWTAuth =
        JWTAuth.create(vertx, JWTAuthOptions().addPubSecKey(publicKey))

    fun asymmetric(vertx: Vertx, publicKey: String, privateKey: String): JWTAuth = JWTAuth.create(
        vertx,
        JWTAuthOptions()
            .addPubSecKey(publicKey)
            .addPubSecKey(privateKey)
    )
}

private fun JWTAuthOptions.addPubSecKey(key: String): JWTAuthOptions =
    addPubSecKey(
        PubSecKeyOptions().apply {
            algorithm = "RS256"
            buffer = Buffer.buffer(key)
        }
    )
