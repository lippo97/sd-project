package it.unibo.lpaas.client.repl

import io.vertx.core.Future

interface Repl {
    fun repl(): Future<Void>
}
