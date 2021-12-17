package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.domain.databind.derive
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.TermParser

val structDeserializer: JsonDeserializer<Struct> by lazy {
    StringDeserializer().derive {
        TermParser.withStandardOperators.parseStruct(it)
    }
}
