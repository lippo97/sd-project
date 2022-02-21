package it.unibo.lpaas.client.streams

import io.vertx.core.Handler
import io.vertx.core.streams.ReadStream

class MappedStream<A, B>(
    private val source: ReadStream<A>,
    private val f: (A) -> B,
) : ReadStream<B> {
    override fun exceptionHandler(handler: Handler<Throwable>): ReadStream<B> = apply {
        source.exceptionHandler(handler)
    }

    override fun handler(handler: Handler<B>): ReadStream<B> = apply {
        source.handler {
            handler.handle(f(it))
        }
    }

    override fun pause(): ReadStream<B> = apply {
        source.pause()
    }

    override fun resume(): ReadStream<B> = apply {
        source.resume()
    }

    override fun fetch(amount: Long): ReadStream<B> = apply {
        source.fetch(amount)
    }

    override fun endHandler(endHandler: Handler<Void>): ReadStream<B> = apply {
        source.endHandler(endHandler)
    }
}

fun <A, B> ReadStream<A>.map(f: (A) -> B): ReadStream<B> = MappedStream(this, f)
