package it.unibo.lpaas.delivery.http.handler.dsl

import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.exception.UnsupportedMediaTypeException
import it.unibo.lpaas.delivery.http.handler.suspendHandler
import it.unibo.lpaas.delivery.http.setStatusCode
import it.unibo.lpaas.http.databind.BufferSerializer
import it.unibo.lpaas.http.databind.MimeType
import it.unibo.lpaas.http.databind.SerializerCollection

interface SerializerDSL {

    val serializerCollection: SerializerCollection<BufferSerializer>

    fun <A> Route.dataHandler(
        returnCode: HTTPStatusCode = HTTPStatusCode.OK,
        fn: suspend (RoutingContext) -> A
    ): Route = suspendHandler { ctx ->
        val result = fn(ctx)
        val contentType = ctx.parseAcceptableContentType()
        val serializer = serializerCollection.serializerForMimeType(contentType)
            ?: throw UnsupportedMediaTypeException(contentType)

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

    private fun RoutingContext.parseAcceptableContentType(): MimeType = runCatching {
        if (acceptableContentType != null)
            MimeType.parse(acceptableContentType)
        else
            MimeType.JSON
    }
        .recoverCatching { if (it is NoSuchElementException) MimeType.JSON else throw it }
        .getOrThrow()
}
