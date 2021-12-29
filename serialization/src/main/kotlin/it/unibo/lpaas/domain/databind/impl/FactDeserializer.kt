package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.databind.derive
import it.unibo.tuprolog.core.parsing.TermParser

val factDeserializer = StringDeserializer().derive {
    val struct = TermParser.withStandardOperators.parseStruct(it)
    Fact.of(struct)
}
