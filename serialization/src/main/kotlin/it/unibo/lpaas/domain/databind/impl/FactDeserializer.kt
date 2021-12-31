package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.databind.derive
import it.unibo.tuprolog.core.parsing.TermParser
import java.io.IOException

val factDeserializer = StringDeserializer().derive {
    val clause = TermParser.withStandardOperators.parseClause(it)
    runCatching {
        Fact.of(clause)
    }.getOrElse {
        throw IOException("The provided clause: $clause is not a Fact.")
    }
}
