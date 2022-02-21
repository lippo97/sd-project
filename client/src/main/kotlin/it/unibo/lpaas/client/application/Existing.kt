package it.unibo.lpaas.client.application

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import it.unibo.lpaas.authentication.domain.Credentials
import it.unibo.lpaas.authentication.domain.Password
import it.unibo.lpaas.authentication.domain.Username
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.client.api.ServerOptions
import it.unibo.lpaas.client.repl.LpaasRepl
import it.unibo.lpaas.domain.TheoryId
import java.util.concurrent.TimeUnit

class Existing : CliktCommand() {
    private val theoryId: TheoryId by argument().convert { TheoryId.of(it) }
    private val options by CommonOptions()

    override fun run() {
        val vertxOptions = VertxOptions().setBlockedThreadCheckIntervalUnit(TimeUnit.DAYS)
        val vertx = Vertx.vertx(vertxOptions)
        val client = vertx.createHttpClient()

        with(RunApplication(vertx, client, options.verbose)) {
            LpaasRepl.fromExistingTheory(
                vertx,
                Lpaas.of(
                    vertx,
                    client,
                    ServerOptions(options.hostname, options.port, "/v1"),
                    Credentials(Username(options.username), Password(options.password))
                ),
                theoryId
            )
                .run()
        }
    }
}
