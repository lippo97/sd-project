package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.auth.Token
import it.unibo.lpaas.delivery.http.auth.TokenStorage
import it.unibo.lpaas.delivery.http.auth.UnauthorizedException
import it.unibo.lpaas.delivery.http.databind.MimeType
import it.unibo.lpaas.delivery.http.setStatusCode

interface AuthController : Controller {
    companion object {
        @JvmStatic
        fun make(vertx: Vertx, jwtProvider: JWTAuth, tokenStorage: TokenStorage): AuthController =
            object : AuthController {
                override fun routes(): Router = Router.router(vertx).apply {
                    post("/login")
                        .handler(BodyHandler.create())
                        .respond { ctx ->
                            val token = Token(ctx.bodyAsString)
                            ctx.response().headers()[HttpHeaders.CONTENT_TYPE] = MimeType.JSON.value
                            tokenStorage.getRole(token).map {
                                jwtProvider.generateToken(
                                    JsonObject().put("groups", listOf(it.value))
                                )
                            }
                        }
                        .failureHandler { ctx ->
                            val throwable = ctx.failure()
                            if (throwable is UnauthorizedException) {
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
