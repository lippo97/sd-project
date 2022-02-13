package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.format

class TermToStringSerializer : JsonSerializer<Term>() {
    override fun serialize(value: Term, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.format(TermFormatter.prettyExpressions()))
    }
}
