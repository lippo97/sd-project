package it.unibo.lpaas.client.repl

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.streams.ReadStream
import it.unibo.lpaas.domain.Result
import java.util.LinkedList

class ResultsHandler(
    private val stream: ReadStream<Result>,
    private val next: () -> Future<Void>
) {
    private val solutions = LinkedList<Result?>()

    private var onNextHandler: Handler<Void>? = null
    private var onEndHandler: Handler<Void>? = null

    fun onResult(handler: Handler<Result>): ResultsHandler = apply {
        stream.handler {
            solutions.add(it)
            if (solutions.size > 1) {
                handler.handle(solutions.removeFirst())
                if (hasNext()) onNextHandler?.handle(null)
                else onEndHandler?.handle(null)
            } else if (hasNext()) {
                next()
            }
        }
    }

    fun onHasNext(handler: Handler<Void>): ResultsHandler = apply {
        onNextHandler = handler
    }

    fun onEnd(handler: Handler<Void>): ResultsHandler = apply {
        onEndHandler = handler
    }

    private fun hasNext(): Boolean = solutions.firstOrNull() != null
}
