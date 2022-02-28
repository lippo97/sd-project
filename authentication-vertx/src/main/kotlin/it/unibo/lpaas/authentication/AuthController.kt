package it.unibo.lpaas.authentication

import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.authentication.domain.Credentials
import it.unibo.lpaas.authentication.provider.CredentialsProvider
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.exception.DeliveryException
import it.unibo.lpaas.delivery.http.handler.dsl.BodyDSL
import it.unibo.lpaas.delivery.http.handler.handleDeliveryException
import it.unibo.lpaas.delivery.http.setStatusCode

interface AuthController : Controller {
    companion object {
        @JvmStatic
        fun make(
            vertx: Vertx,
            jwtProvider: JWTAuth,
            credentialsProvider: CredentialsProvider,
            jwtAlgorithm: String = "HS256"
        ): AuthController =
            object : AuthController {

                override fun routes(): Router = Router.router(vertx).apply {
                    with(BodyDSL.of(BodyHandler.create())) {
                        post("/login")
                            .bodyHandler()
                            .handler { ctx ->
                                val (username, password) = Json.decodeValue(ctx.bodyAsString, Credentials::class.java)
                                credentialsProvider.login(username, password)
                                    .map {
                                        jwtProvider.generateToken(
                                            JsonObject().put("groups", listOf(it.value)),
                                            JWTOptions().setAlgorithm(jwtAlgorithm)
                                        )
                                    }
                                    .onSuccess {
                                        ctx.response()
                                            .end(it)
                                    }
                                    .onFailure(ctx::fail)
                            }
                            .failureHandler { ctx ->
                                val failure = ctx.failure()
                                failure.printStackTrace()
                                if (failure is DeliveryException) {
                                    ctx.handleDeliveryException(failure)
                                } else {
                                    ctx.response()
                                        .setStatusCode(HTTPStatusCode.INTERNAL_SERVER_ERROR)
                                        .end()
                                }
                            }
                    }
                }
            }
    }
}
