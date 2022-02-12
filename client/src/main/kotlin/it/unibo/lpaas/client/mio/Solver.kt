package it.unibo.lpaas.client.mio

import io.vertx.core.Future
import io.vertx.core.streams.ReadStream

interface Solver<A, B> {
    fun solve(t: A): Future<ReadStream<B>>

    companion object {
        fun <A, B> of(fn: (A) -> Future<ReadStream<B>>): Solver<A, B> = object : Solver<A, B> {
            override fun solve(t: A): Future<ReadStream<B>> = fn(t)
        }
    }
}
