package it.unibo.lpaas.client.api

import io.vertx.core.Future
import io.vertx.core.http.WebSocket
import io.vertx.core.json.Json
import io.vertx.core.streams.ReadStream
import it.unibo.lpaas.client.streams.map
import it.unibo.lpaas.domain.Result

interface ResultStream : ReadStream<Result> {

    fun next(): Future<Void>

    companion object {

        fun of(ws: WebSocket): ResultStream {
            val resultStream = ws.map { Json.decodeValue(it, Result::class.java) }
            return object : ReadStream<Result> by resultStream, ResultStream {
                override fun next(): Future<Void> =
                    ws.writeTextMessage("get")
            }
        }
    }
}
