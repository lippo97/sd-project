package it.unibo.lpaas.delivery.http.handler

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.lpaas.core.UseCase
import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NonFatalError
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.MimeType
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.setStatusCode
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
 * The function passed as an argument is called inside a `runCatching` block,
 * that catches domain-specific errors, handling them gracefully; fatal errors
 * will result in a fail with an internal error instead.
 *
 * @note: This function is meant as an internal DSL to factor out code that
 * would be duplicated otherwise.
 */
internal fun Route.suspendHandler(fn: suspend (RoutingContext) -> Unit): Route =
    handler { ctx ->
        GlobalScope.launch(ctx.vertx().dispatcher()) {
            runCatching { fn(ctx) }
                .onFailure {
                    if (System.getProperty("Environment") == "Dev") {
                        it.printStackTrace()
                    }
                }
                .recoverCatching {
                    if (it is NonFatalError) handleNonFatal(ctx, it) else throw it
                }
                .onFailure(ctx::fail)
        }
    }

/**
 * Helper DSL method that allows to work with [UseCase]s.
 */
internal fun <T> Route.useCaseHandler(
    mapper: Map<MimeType, BufferSerializer>,
    returnCode: HTTPStatusCode = HTTPStatusCode.OK,
    fn: suspend (RoutingContext) -> UseCase<T>
): Route =
    suspendHandler { ctx ->
        fn(ctx).execute().let { result ->
            val contentType = runCatching {
                if (ctx.acceptableContentType != null)
                    MimeType.parse(ctx.acceptableContentType)
                else
                    MimeType.JSON
            }
                .recoverCatching { if (it is NoSuchElementException) MimeType.JSON else throw it }
                .getOrThrow()

            // Default it with a Json factory in order to let it not crash
            val serializer = mapper[contentType] ?: run {
                println("Coudln't find the requested serializer.")
                BufferSerializer.of(
                    ObjectMapper(JsonFactory()).enable(SerializationFeature.INDENT_OUTPUT).writer()
                )
            }

            if (result != Unit) {
                ctx.response()
                    .setStatusCode(returnCode)
                    .putHeader(HttpHeaders.CONTENT_TYPE, contentType.value)
                    .end(serializer.serializeToBuffer(result))
            } else {
                ctx.response()
                    .setStatusCode(HTTPStatusCode.NO_CONTENT)
                    .end()
            }
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
