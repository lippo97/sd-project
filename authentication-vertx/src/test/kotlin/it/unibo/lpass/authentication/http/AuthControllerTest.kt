package it.unibo.lpass.authentication.http

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
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
import it.unibo.lpaas.authentication.AuthController
import it.unibo.lpaas.authentication.provider.Credentials
import it.unibo.lpaas.authentication.provider.CredentialsProvider
import it.unibo.lpaas.authentication.provider.Password
import it.unibo.lpaas.authentication.provider.Username
import it.unibo.lpaas.authentication.serialization.PasswordDeserializer
import it.unibo.lpaas.authentication.serialization.PasswordSerializer
import it.unibo.lpaas.authentication.serialization.UsernameDeserializer
import it.unibo.lpaas.authentication.serialization.UsernameSerializer
import it.unibo.lpaas.http.databind.SerializerCollection
import it.unibo.lpaas.http.databind.SerializerConfiguration

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

    val serializerCollection = SerializerCollection.default()

    SerializerConfiguration.defaultWithModule {
        addSerializer(Username::class.java, UsernameSerializer())
        addSerializer(Password::class.java, PasswordSerializer())
        addDeserializer(Username::class.java, UsernameDeserializer())
        addDeserializer(Password::class.java, PasswordDeserializer())
    }
        .applyOnJacksonAndSerializers(serializerCollection)

    suspend fun withHttpServerOf(credentialsProvider: CredentialsProvider, fn: suspend () -> Unit) {
        with(
            vertx.createHttpServer()
                .requestHandler(AuthController.make(vertx, jwtProvider, credentialsProvider).routes())
        ) {
            listen(8080).await()
            fn()
            close().await()
        }
    }

    val sampleCredentials = Credentials(Username("abc"), Password("pass"))

    val goodProvider = CredentialsProvider.inMemory(
        sampleCredentials to Role.CONFIGURATOR
    )

    val badProvider = CredentialsProvider.inMemory()

    context("When a user tries to log in") {
        test("it should return the token") {
            withHttpServerOf(goodProvider) {
                client.request(HttpMethod.POST, 8080, "localhost", "/login")
                    .flatMap { it.send(Json.encode(sampleCredentials)) }
                    .map { it.statusCode() shouldBe 200; it }
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
            withHttpServerOf(badProvider) {
                client.request(HttpMethod.POST, 8080, "localhost", "/login")
                    .flatMap { it.send(Json.encode(sampleCredentials)) }
                    .map { it.statusCode() }
                    .map {
                        it shouldBeExactly 401
                    }
                    .await()
            }
        }
    }
})
