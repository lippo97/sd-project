package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.http.ServerWebSocket

fun ServerWebSocket.onMessage(msg: String, fn: () -> Unit) {
    handler { recv ->
        if (recv.toString() == msg) {
            fn()
        }
    }
}
