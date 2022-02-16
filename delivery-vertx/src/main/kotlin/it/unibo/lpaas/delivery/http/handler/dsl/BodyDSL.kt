package it.unibo.lpaas.delivery.http.handler.dsl

import io.vertx.ext.web.Route
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.core.exception.ValidationException

interface BodyDSL {
    val bodyHandler: BodyHandler

    fun Route.bodyHandler(): Route = handler(bodyHandler::handle)
        .handler { ctx ->
            if (ctx.body == null)
                ctx.fail(ValidationException(message = "Body can not be empty"))
            else
                ctx.next()
        }

    companion object {
        fun of(bodyHandler: BodyHandler): BodyDSL = object : BodyDSL {
            override val bodyHandler: BodyHandler = bodyHandler
        }
    }
}
