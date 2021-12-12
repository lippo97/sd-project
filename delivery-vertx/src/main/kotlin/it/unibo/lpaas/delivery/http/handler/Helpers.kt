package it.unibo.lpaas.delivery.http.handler

import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NonFatalError
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.setStatusCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
        GlobalScope.launch(ctx.vertx().dispatcher()) {
            runCatching { fn(ctx) }
                .recover {
                    if (it is NonFatalError) handleNonFatal(ctx, it) else throw it
                }
                .onFailure(ctx::fail)
        }
    }
}

fun handleNonFatal(ctx: RoutingContext, error: NonFatalError) {
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
