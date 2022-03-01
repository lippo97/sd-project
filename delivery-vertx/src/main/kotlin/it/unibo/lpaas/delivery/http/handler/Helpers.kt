package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.lpaas.core.exception.CoreException
import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.exception.DeliveryException
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import it.unibo.lpaas.delivery.http.exception.UnsupportedMediaTypeException
import it.unibo.lpaas.delivery.http.exception.ValidationException
import it.unibo.lpaas.delivery.http.setStatusCode
import it.unibo.lpaas.http.databind.MimeType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
    blockingHandler { ctx ->
        GlobalScope.launch(ctx.vertx().dispatcher()) {
            runCatching {
                fn(ctx)
            }
                .getOrElse { ctx.fail(it) }
        }
    }

internal fun RoutingContext.handleCoreException(error: CoreException) {
    val statusCode = when (error) {
        is NotFoundException -> HTTPStatusCode.NOT_FOUND
        is DuplicateIdentifierException -> HTTPStatusCode.CONFLICT
    }
    val response = response()
        .setStatusCode(statusCode)

    if (error.message != null) response.end(error.message)
    else response.end()
}

fun RoutingContext.handleDeliveryException(error: DeliveryException) {
    val statusCode = when (error) {
        is UnauthorizedException -> HTTPStatusCode.UNAUTHORIZED
        is ValidationException -> HTTPStatusCode.BAD_REQUEST
        is UnsupportedMediaTypeException -> HTTPStatusCode.UNSUPPORTED_MEDIA_TYPE
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
