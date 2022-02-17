package it.unibo.lpaas.authentication

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.authentication.provider.CredentialsProvider
import it.unibo.lpaas.authentication.provider.Password
import it.unibo.lpaas.authentication.provider.Username
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import it.unibo.lpaas.delivery.http.handler.dsl.BodyDSL
import it.unibo.lpaas.delivery.http.setStatusCode

interface AuthController : Controller {
    companion object {
        @JvmStatic
        fun make(vertx: Vertx, jwtProvider: JWTAuth, credentialsStorage: CredentialsProvider): AuthController =
            object : AuthController {

                override fun routes(): Router = Router.router(vertx).apply {
                    with(BodyDSL.of(BodyHandler.create())) {
                        post("/login")
                            .bodyHandler()
                            .handler { ctx ->
                                val username = Username(ctx.bodyAsJson.getString("username"))
                                val password = Password(ctx.bodyAsJson.getString("password"))

                                credentialsStorage.login(username, password)
                                    .map {
                                        jwtProvider.generateToken(
                                            JsonObject().put("groups", listOf(it.value))
                                        )
                                    }
                                    .onSuccess {
                                        ctx.response()
                                            .end(it)
                                    }
                                    .onFailure { failure ->
                                        if (failure is UnauthorizedException) {
                                            ctx.response()
                                                .setStatusCode(HTTPStatusCode.UNAUTHORIZED)
                                                .end()
                                        } else {
                                            ctx.next()
                                        }
                                    }
                            }
                    }
                }
            }
    }
}
