package it.unibo.lpaas.client

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.jackson.DatabindCodec
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.client.mio.AsyncRepl2
import it.unibo.lpaas.client.mio.ServerOptions
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.SerializerConfiguration
import it.unibo.tuprolog.theory.parsing.parse

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

    val lpaas = Lpaas.of(
        vertx,
        client,
        ServerOptions("localhost", 8080, "/v1"),
        "abc2"
    )

    val theory2p = it.unibo.tuprolog.theory.Theory.parse(
        """
        elem(X, [X|_]).
        elem(X, [_|Xs]) :- elem(X, Xs).
        """.trimIndent()
    )
//    val theory2p = it.unibo.tuprolog.theory.Theory.of(
//        Clause.of(Struct.of("ciao", Atom.of("mario"))),
//        Clause.of(Struct.of("ciao", Atom.of("luigi"))),
//        Clause.of(Struct.of("ciao", Atom.of("peach"))),
//    )

    fun createAsyncRepl(theoryId: TheoryId): Future<AsyncRepl2> =
        AsyncRepl2.fromExistingTheory(
            vertx,
            Lpaas.of(
                vertx,
                client,
                ServerOptions("localhost", 8080, "/v1"),
                "abc2"
            ),
            theoryId
        )

    val theoryName = TheoryId.of("exampleTheory")
    lpaas.createTheory(theoryName, Theory.Data(theory2p))
        .compose(
            { createAsyncRepl(it.name) },
            { createAsyncRepl(theoryName) }
        )
        .flatMap {
            it.repl()
        }
        .onFailure {
            it.printStackTrace()
        }
}
