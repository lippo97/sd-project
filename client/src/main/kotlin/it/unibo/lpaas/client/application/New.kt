package it.unibo.lpaas.client.application

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.types.file
import io.vertx.core.Vertx
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.client.api.ServerOptions
import it.unibo.lpaas.client.repl.LpaasRepl
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse

class New : CliktCommand() {
    val file by argument().file(mustExist = true)
    val options by CommonOptions()

    override fun run() {
        val vertx = Vertx.vertx()
        val client = vertx.createHttpClient()

        with(RunApplication(vertx, client, options.verbose)) {
            vertx.fileSystem().readFile(file.absolutePath)
                .map { Theory.parse(it.toString()) }
                .flatMap {
                    LpaasRepl.fromTheory(
                        vertx,
                        Lpaas.of(
                            vertx,
                            client,
                            ServerOptions(options.hostname, options.port, "/v1"),
                            options.accessToken,
                        ),
                        it,
                    )
                }
                .run()
        }
    }
}
