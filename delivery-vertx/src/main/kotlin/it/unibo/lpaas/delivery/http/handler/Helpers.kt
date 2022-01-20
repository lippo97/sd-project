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
        // Since apparently exceptions thrown inside this handler aren't
        // propagated to the vertx error handler automatically, we had to do it
        // manually.
        GlobalScope.launch(ctx.vertx().dispatcher()) {
            runCatching {
                fn(ctx)
            }
                .getOrElse { ctx.fail(it) }
        }
    }

internal fun RoutingContext.handleNonFatal(error: NonFatalError) {
    val statusCode = when (error) {
        is NotFoundException -> HTTPStatusCode.NOT_FOUND
        is DuplicateIdentifierException -> HTTPStatusCode.CONFLICT
        is ValidationException -> HTTPStatusCode.BAD_REQUEST
        else -> HTTPStatusCode.INTERNAL_SERVER_ERROR
    }
    val response = response()
        .setStatusCode(statusCode)

    if (error.message != null) response.end(error.message)
    else response.end()
}

internal fun Route.produces(item: MimeType): Route = apply {
    produces(listOf(item))
}

internal fun Route.produces(items: Collection<MimeType>): Route = apply {
    items.forEach { produces(it.value) }
}
