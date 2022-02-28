package it.unibo.lpaas.client.application

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import it.unibo.lpaas.client.api.exception.HTTPException
import it.unibo.lpaas.client.repl.Repl

class RunApplication(
    private val vertx: Vertx,
    private val client: HttpClient,
    private val verbose: Boolean = false
) {
    private fun errorHandler(failure: Throwable) {
        if (verbose) {
            failure.printStackTrace()
        }
        if (failure is HTTPException) {
            if (failure.statusCode == HttpResponseStatus.UNAUTHORIZED.code()) {
                println("The provided token is expired or invalid.")
            } else if (failure.statusCode == HttpResponseStatus.FORBIDDEN.code()) {
                println("You don't have the permission to perform the requested operation.")
            } else println(failure.message)
        } else {
            println(failure.message)
        }
        @Suppress("MagicNumber")
        vertx.setTimer(100) {
            client.close()
                .flatMap { vertx.close() }
        }
    }

    fun Future<Repl>.run(): Future<Void> =
        flatMap { it.repl() }
            .onFailure(::errorHandler)
}
