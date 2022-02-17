package it.unibo.lpaas.client.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.Json
import it.unibo.lpaas.authentication.provider.Credentials
import it.unibo.lpaas.client.api.exception.UnauthorizedException

interface JwtTokenAuthentication {
    fun getValidToken(): Future<JwtToken>

    fun invalidateToken()

    companion object {
        @Suppress("TooGenericExceptionThrown")
        fun usingToken(
            client: HttpClient,
            serverOptions: ServerOptions,
            credentials: Credentials
        ): JwtTokenAuthentication = object : JwtTokenAuthentication {

            var cached: JwtToken? = null

            fun login(): Future<JwtToken> =
                client.request(
                    RequestOptions().apply {
                        host = serverOptions.hostname
                        port = serverOptions.port
                        method = HttpMethod.POST
                        uri = "/login"
                    }
                )
                    .flatMap { req ->
                        req.end(Json.encode(credentials))
                        req.response()
                    }
                    .flatMap { res ->
                        val body =
                            if (res.statusCode() == HttpResponseStatus.UNAUTHORIZED.code()) {
                                Future.failedFuture(UnauthorizedException())
                            } else if (res.statusCode() != HttpResponseStatus.OK.code()) {
                                Future.failedFuture(RuntimeException("HTTP Status Code = ${res.statusCode()}"))
                            } else
                                res.body()
                        body.onFailure { res.end() }
                    }
                    .map { JwtToken(it.toString()) }

            override fun getValidToken(): Future<JwtToken> =
                if (cached == null) {
                    login()
                        .onSuccess { cached = it }
                } else Future.succeededFuture(cached)

            override fun invalidateToken() {
                cached = null
            }
        }
    }
}
