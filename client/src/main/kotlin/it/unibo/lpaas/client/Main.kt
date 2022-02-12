package it.unibo.lpaas.client

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.jackson.DatabindCodec
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.client.repl.LpaasRepl
import it.unibo.lpaas.client.api.ServerOptions
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.SerializerConfiguration
import it.unibo.tuprolog.theory.parsing.parse

/*
 * --hostname <host>
 * --port <port>
 * --theory-id <theoryId>
 * --theory-file <path-to-theory.pl>
 */
@Suppress("MagicNumber")
fun main() {
    val vertx = Vertx.vertx(VertxOptions().setWorkerPoolSize(16))
    val client = vertx.createHttpClient()

    SerializerConfiguration.defaultWithModule {
        addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
        addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
        addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
        addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
    }.applyOn(listOf(DatabindCodec.prettyMapper(), DatabindCodec.mapper()))

    val theory2p = it.unibo.tuprolog.theory.Theory.parse(
        """
        elem(X, [X|_]).
        elem(X, [_|Xs]) :- elem(X, Xs).
        """.trimIndent()
    )

    LpaasRepl.fromTheory(
        vertx,
        Lpaas.of(
            vertx,
            client,
            ServerOptions("localhost", 8080, "/v1"),
            "abc2"
        ),
        theory2p,
    )
        .flatMap {
            it.repl()
        }
        .onFailure {
            it.printStackTrace()
        }
}
