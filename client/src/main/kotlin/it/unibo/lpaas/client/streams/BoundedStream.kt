package it.unibo.lpaas.client.streams

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.streams.ReadStream
import io.vertx.core.streams.WriteStream
import io.vertx.core.streams.impl.InboundBuffer

@Suppress("MagicNumber", "TooManyFunctions")
class BoundedStream<T>(
    vertx: Vertx,
) : ReadStream<T>, WriteStream<T> {
    private var isClosed: Boolean = false
    private var maxWrites: Int = 10
    private var endHandler: Handler<Void>? = null
    private val queue: InboundBuffer<T> = InboundBuffer(vertx.orCreateContext)

    override fun exceptionHandler(handler: Handler<Throwable>): BoundedStream<T> = apply {
        check()
        queue.exceptionHandler(handler)
    }

    override fun handler(handler: Handler<T>): BoundedStream<T> = apply {
        check()
        queue.handler(handler)
    }

    override fun pause(): BoundedStream<T> = apply {
        check()
        queue.pause()
    }

    override fun resume(): BoundedStream<T> = apply {
        check()
        queue.resume()
    }

    override fun fetch(amount: Long): BoundedStream<T> = apply {
        check()
        queue.fetch(amount)
    }

    override fun endHandler(endHandler: Handler<Void>): BoundedStream<T> = apply {
        check()
        this.endHandler = endHandler
    }

    private fun check() {
        if (isClosed) {
            throw IllegalStateException("Stream was already closed")
        }
    }

    override fun write(data: T): Future<Void> {
        check()
        val promise = Promise.promise<Void>()
        write(data, promise)
        return promise.future()
    }

    override fun write(data: T, handler: Handler<AsyncResult<Void>>) {
        check()
        if (queue.write(data)) {
            handler.handle(Future.succeededFuture())
        } else {
            handler.handle(Future.failedFuture(IllegalStateException("queue.write(data) -> false")))
        }
    }

    override fun end(handler: Handler<AsyncResult<Void>>) {
        check()
        isClosed = true
        endHandler?.handle(null)
        handler.handle(Future.succeededFuture())
    }

    override fun drainHandler(handler: Handler<Void>?): BoundedStream<T> = apply {
        queue.drainHandler(handler)
    }

    override fun setWriteQueueMaxSize(maxSize: Int): BoundedStream<T> = apply {
        require(maxSize >= 2) { "maxSize must be >= 2" }
        check()
        this.maxWrites = maxSize
    }

    override fun writeQueueFull(): Boolean =
        queue.size() > maxWrites
}
