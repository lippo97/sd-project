package it.unibo.lpaas.delivery.http

import io.vertx.core.http.HttpServerResponse

@Suppress("MagicNumber")
enum class HTTPStatusCode(val code: Int) {
    OK(200),
    CREATED(201),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    INTERNAL_SERVER_ERROR(500),
}

fun HttpServerResponse.setStatusCode(statusCode: HTTPStatusCode): HttpServerResponse =
    setStatusCode(statusCode.code)
