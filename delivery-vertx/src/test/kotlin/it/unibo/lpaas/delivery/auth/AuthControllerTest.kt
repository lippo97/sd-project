package it.unibo.lpaas.delivery.auth

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.delivery.http.auth.Token
import it.unibo.lpaas.delivery.http.auth.TokenStorage
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import it.unibo.lpaas.delivery.http.handler.AuthController

@Tags("HTTP")
class AuthControllerTest : FunSpec({

    val vertx = Vertx.vertx()
    val client = vertx.createHttpClient()

    val jwtProvider = JWTAuth.create(
        vertx,
        JWTAuthOptions()
            .addPubSecKey(
                PubSecKeyOptions()
                    .setAlgorithm("HS256")
                    .setBuffer("keyboard cat")
            )
    )

    suspend fun withHttpServerOf(tokenStorage: TokenStorage, fn: suspend () -> Unit) {
        with(
            vertx.createHttpServer()
                .requestHandler(AuthController.make(vertx, jwtProvider, tokenStorage).routes())
        ) {
            listen(8080).await()
            fn()
            close().await()
        }
    }

    val goodStorage = object : TokenStorage {
        override fun getRole(token: Token): Future<Role> = Future.succeededFuture(Role.CONFIGURATOR)
    }

    val badStorage = object : TokenStorage {
        override fun getRole(token: Token): Future<Role> = Future.failedFuture(UnauthorizedException())
    }

    context("When a user tries to log in") {
        test("it should return the token") {
            withHttpServerOf(goodStorage) {
                client.request(HttpMethod.POST, 8080, "localhost", "/login")
                    .flatMap { it.send("goodToken") }
                    .flatMap { it.body() }
                    .flatMap {
                        jwtProvider.authenticate(json { obj("token" to it.toString()) })
                    }
                    .map {
                        val groups: JsonArray = it.principal()["groups"]
                        groups shouldBe json { array("configurator") }
                    }
                    .await()
            }
        }
        test("it should return unauthorized error") {
            withHttpServerOf(badStorage) {
                client.request(HttpMethod.POST, 8080, "localhost", "/login")
                    .flatMap { it.send("fakeToken") }
                    .map { it.statusCode() }
                    .map {
                        it shouldBeExactly 401
                    }
                    .await()
            }
        }
    }
})
