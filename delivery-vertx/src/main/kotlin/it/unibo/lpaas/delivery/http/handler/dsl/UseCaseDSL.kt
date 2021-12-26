package it.unibo.lpaas.delivery.http.handler.dsl

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.auth.authorization.OrAuthorization
import io.vertx.ext.auth.authorization.RoleBasedAuthorization
import io.vertx.ext.auth.jwt.authorization.MicroProfileAuthorization
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.AuthorizationHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.core.UseCase
import it.unibo.lpaas.core.exception.NonFatalError
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.delivery.http.databind.MimeType
import it.unibo.lpaas.delivery.http.databind.ObjectMapperSerializer
import it.unibo.lpaas.delivery.http.handler.USE_CASE_CTX
import it.unibo.lpaas.delivery.http.handler.handleNonFatal
import it.unibo.lpaas.delivery.http.handler.suspendHandler
import it.unibo.lpaas.delivery.http.setStatusCode

internal interface UseCaseDSL {

    val mimeMap: MimeMap<BufferSerializer>

    val authorizationProvider: AuthorizationProvider

    fun <T> Route.useCaseHandler(
        returnCode: HTTPStatusCode = HTTPStatusCode.OK,
        fn: (RoutingContext) -> UseCase<T>,
    ): Route = apply {
        createUseCase(fn)
        createAuthorizationHandler()
        suspendHandler { ctx ->
            val useCase: UseCase<*> = ctx[USE_CASE_CTX]

            useCase.execute().let { result ->
                val contentType = ctx.parseAcceptableContentType()

                // Default it with a Json factory in order to let it not crash
                val serializer = mimeMap.serializerOrDefault(
                    contentType,
                    ObjectMapperSerializer.of(
                        ObjectMapper(JsonFactory()).enable(SerializationFeature.INDENT_OUTPUT)
                    )
                )

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
    }

    private fun <T> Route.createUseCase(fn: (RoutingContext) -> UseCase<T>) =
        handler { ctx ->
            runCatching {
                ctx.put(USE_CASE_CTX, fn(ctx))
                ctx.next()
            }
                .onFailure {
                    if (it is NonFatalError) handleNonFatal(ctx, it)
                    else ctx.fail(it)
                }
        }

    private fun Route.createAuthorizationHandler() = handler { ctx ->
        val useCase: UseCase<*> = ctx[USE_CASE_CTX]
        AuthorizationHandler.create(
            OrAuthorization.create().apply {
                authorizationProvider.authorizedRoles(useCase.tag).forEach {
                    addAuthorization(RoleBasedAuthorization.create(it.value))
                }
            }
        )
            .addAuthorizationProvider(MicroProfileAuthorization.create())
            .handle(ctx)
    }

    private fun RoutingContext.parseAcceptableContentType(): MimeType = runCatching {
        if (acceptableContentType != null)
            MimeType.parse(acceptableContentType)
        else
            MimeType.JSON
    }
        .recoverCatching { if (it is NoSuchElementException) MimeType.JSON else throw it }
        .getOrThrow()

    companion object {
        fun of(mimeMap: MimeMap<BufferSerializer>, authorizationProvider: AuthorizationProvider): UseCaseDSL =
            object : UseCaseDSL {
                override val mimeMap: MimeMap<BufferSerializer> = mimeMap

                override val authorizationProvider: AuthorizationProvider = authorizationProvider
            }
    }
}