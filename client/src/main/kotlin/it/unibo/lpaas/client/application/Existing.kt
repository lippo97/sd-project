package it.unibo.lpaas.client.application

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import io.vertx.core.Vertx
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.client.api.ServerOptions
import it.unibo.lpaas.client.repl.LpaasRepl
import it.unibo.lpaas.domain.TheoryId

class Existing : CliktCommand() {
    val theoryId: TheoryId by argument().convert { TheoryId.of(it) }
    val options by CommonOptions()

    override fun run() {
        val vertx = Vertx.vertx()
        val client = vertx.createHttpClient()

        with(RunApplication(vertx, client, options.verbose)) {
            LpaasRepl.fromExistingTheory(
                vertx,
                Lpaas.of(
                    vertx,
                    client,
                    ServerOptions(options.hostname, options.port, "/v1"),
                    options.accessToken
                ),
                theoryId
            )
                .run()
        }
    }
}
