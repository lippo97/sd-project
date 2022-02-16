package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.auth.Token
import it.unibo.lpaas.delivery.http.auth.TokenStorage
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import it.unibo.lpaas.delivery.http.handler.dsl.BodyDSL
import it.unibo.lpaas.delivery.http.setStatusCode

interface AuthController : Controller {
    companion object {
        @JvmStatic
        fun make(vertx: Vertx, jwtProvider: JWTAuth, tokenStorage: TokenStorage): AuthController =
            object : AuthController {

                override fun routes(): Router = Router.router(vertx).apply {
                    with(BodyDSL.of(BodyHandler.create())) {
                        post("/login")
                            .bodyHandler()
                            .handler { ctx ->
                                val token = Token(ctx.bodyAsString)
                                tokenStorage.getRole(token).map {
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
