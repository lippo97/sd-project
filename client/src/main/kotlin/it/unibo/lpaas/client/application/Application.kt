package it.unibo.lpaas.client.application

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import io.vertx.core.json.jackson.DatabindCodec
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.SerializerConfiguration

fun main(args: Array<String>) {
    SerializerConfiguration.defaultWithModule {
        addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
        addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
        addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
        addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
    }.applyOn(listOf(DatabindCodec.prettyMapper(), DatabindCodec.mapper()))

    NoOpCliktCommand()
        .subcommands(Existing(), New())
        .main(args)
}
