package it.unibo.lpaas.client.mio

import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec.mapper
import io.vertx.core.json.jackson.DatabindCodec.prettyMapper
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.SerializerConfiguration

@Suppress("UnusedPrivateMember")
class Repl(private val solver: Solver<Int, String>) {
    tailrec fun loop() {
        print("?- ")
        val read = readLine() ?: ""
        if (read != "exit") {
//            val results = solver.solve(Integer.parseInt(read)).iterator()
            val results = iterator {
                yield("ciao")
            }
            resultsLoop(results)
            loop()
        }
    }

    private fun resultsLoop(results: Iterator<String>) {
        tailrec fun go(results: Iterator<String>) {
            println(results.next())
            if (results.hasNext() && readLine() == ";") {
                go(results)
            }
        }
        if (results.hasNext()) go(results)
    }
}

fun main() {
    val vertx = Vertx.vertx()
    val client = vertx.createHttpClient()

    SerializerConfiguration.defaultWithModule {
        addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
        addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
        addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
        addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
    }.applyOn(listOf(prettyMapper(), mapper()))
}
