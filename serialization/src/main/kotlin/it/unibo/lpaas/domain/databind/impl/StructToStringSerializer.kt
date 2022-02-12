package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.format

class StructToStringSerializer : JsonSerializer<Struct>() {
    override fun serialize(value: Struct, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.format(TermFormatter.prettyExpressions()))
    }
}
