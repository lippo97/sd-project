package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NonFatalError
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.databind.MimeType
import it.unibo.lpaas.delivery.http.setStatusCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val USE_CASE_CTX = "USE_CASE"

/**
 * Lifts the existing `JSON.decodeValue` function, mapping its errors into
 * domain-specific ones.
 *
 * @throws ValidationException
 */
internal fun <T> decodeJson(buffer: Buffer, clazz: Class<T>): T =
    runCatching { Json.decodeValue(buffer, clazz) }
        .recoverCatching {
            when (it) {
                is ClassCastException -> throw ValidationException("Body parse error", it.cause)
                is DecodeException -> throw ValidationException("Body parse error", it.cause)
                else -> throw it
            }
        }
        .getOrThrow()

/**
 * Helper method that allows to call suspend functions inside a VertX handler.
 * Coroutines will be executed into the default VertX coroutine dispatcher.
 *
 * @note: This function is meant as an internal DSL to factor out code that
 * would be duplicated otherwise.
 */
internal fun Route.suspendHandler(fn: suspend (RoutingContext) -> Unit): Route =
    handler { ctx ->
        GlobalScope.launch(ctx.vertx().dispatcher()) {
            fn(ctx)
        }
    }

internal fun handleNonFatal(ctx: RoutingContext, error: NonFatalError) {
    when (error) {
        is NotFoundException -> ctx.response()
            .setStatusCode(HTTPStatusCode.NOT_FOUND)
            .end(error.message)
        is DuplicateIdentifierException -> ctx.response()
            .setStatusCode(HTTPStatusCode.CONFLICT)
            .end(error.message)
        is ValidationException -> ctx.response()
            .setStatusCode(HTTPStatusCode.BAD_REQUEST)
            .end(error.message)
        else -> ctx.response()
            .setStatusCode(HTTPStatusCode.INTERNAL_SERVER_ERROR)
            .end(error.message)
    }
}

internal fun Route.produces(items: Collection<MimeType>): Route = apply {
    items.forEach { produces(it.value) }
}
