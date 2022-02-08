package it.unibo.lpaas.client.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import it.unibo.lpaas.client.api.exception.UnauthorizedException
import it.unibo.lpaas.client.mio.ServerOptions

interface JwtTokenAuthentication {
    fun getValidToken(): Future<JwtToken>

    fun invalidateToken()

    companion object {
        @Suppress("TooGenericExceptionThrown")
        fun usingToken(
            client: HttpClient,
            serverOptions: ServerOptions,
            token: String
        ): JwtTokenAuthentication = object : JwtTokenAuthentication {

            var cached: JwtToken? = null

            fun login(): Future<JwtToken> =
                client.request(
                    RequestOptions().apply {
                        host = serverOptions.hostname
                        port = serverOptions.port
                        method = HttpMethod.POST
                        uri = "${serverOptions.baseUrl}/login"
                    }
                )
                    .flatMap { it.send(token) }
                    .flatMap {
                        if (it.statusCode() == HttpResponseStatus.UNAUTHORIZED.code()) {
                            throw UnauthorizedException()
                        } else if (it.statusCode() != HttpResponseStatus.OK.code()) {
                            throw RuntimeException("HTTP Status Code = ${it.statusCode()}")
                        }
                        it.body()
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
